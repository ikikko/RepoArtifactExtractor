package org.ikikko

import org.cyberneko.html.parsers.SAXParser
import org.ikikko.writer.ConsoleArtifactWriter
import org.ikikko.writer.ExcelArtifactWriter
import org.ikikko.writer.TracArtifactWriter

class RepoArtifactExtractor {

	// 実行方法
	static def usage = '''
Usage   : gradle run '\${excel file}' '\${extract url} '\${trac url}'
Example : gradle run artifacts.xls http://maven.seasar.org/maven2/org/seasar/cubby/ http://localhost:8080/trac/
'''

	// HTMLスクレイピング用パターン
	static final def DIR_PATTERN = ~/[^?\/]+\//
	static final def POM_PATTERN = ~/.+\.pom/

	def writers

	public static void main(String[] args) {
		new RepoArtifactExtractor().execute(args)
	}

	def execute(args) {
		// 引数セット
		if (args.length != 3) {
			System.err.println usage
			System.exit 1
		}
		def excel = args[0]
		def baseUrl = args[1]
		def tracUrl = args[2]

		writers = [
			new ConsoleArtifactWriter(),
			new ExcelArtifactWriter(excel),
			new TracArtifactWriter(tracUrl),
		]

		// Main
		writers.each { it.init() }
		traverseDir(baseUrl)
		writers.each { it.close() }
	}

	/**
	 * ディレクトリをトラバースする
	 */
	def traverseDir(url) {
		def parser = new XmlSlurper(new SAXParser())
		def html = parser.parse(url)

		def links = html.'**'.findAll{
			// トラバーサルロジックは色々変わるかも
			it.name() == 'A' &&
					!it.@href.toString().startsWith('.') &&
					DIR_PATTERN.matcher(it.@href.toString()).matches()
		}

		// バージョン階層のディレクトリまでトラバースしたら、最新のバージョンのみ対象とする
		def first = links[0]
		if (isPomExistsDir("${url}${first.@href}")) {
			def latest = links[-1]
			findPom("${url}${latest.@href}")
		} else {
			links.each { traverseDir("${url}${it.@href}") }
		}
	}

	/**
	 * POMが存在するディレクトリか否かを判定する
	 */
	def isPomExistsDir(url) {
		def parser = new XmlSlurper(new SAXParser())
		def html = parser.parse(url)

		return html.'**'.any {
			it.name() == 'A' && POM_PATTERN.matcher(it.@href.toString()).matches()
		}
	}

	/**
	 * POMを検索する
	 */
	def findPom(url) {
		def parser = new XmlSlurper(new SAXParser())
		def html = parser.parse(url)

		def pom = html.'**'.find {
			it.name() == 'A' && POM_PATTERN.matcher(it.@href.toString()).matches()
		}
		parsePom("${url}${pom.@href}")
	}

	/**
	 * POMをパースする
	 */
	def parsePom(url) {
		def pom = new XmlSlurper().parse(url);

		def groupId = pom.groupId.isEmpty() ? pom.parent.groupId : pom.groupId
		def artifactId = pom.artifactId
		def version = pom.version.isEmpty() ? pom.parent.version : pom.version

		writers.each { it.write(groupId, artifactId, version, extractArtifactUrl(url)) }
	}

	/**
	 * POMのURLからアーティファクトURLを抽出する
	 */
	def extractArtifactUrl(pomUrl) {
		def paths = pomUrl.tokenize('/')
		def artifactUrl = (pomUrl - paths[-1] - paths[-2])[0..-2]

		return artifactUrl
	}
}