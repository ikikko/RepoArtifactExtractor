package org.ikikko

import groovy.transform.Canonical

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
	@Canonical
	static class Artifact implements Comparable {
		def groupId
		def artifactId

		@Override
		public int compareTo(def o) {
			"${groupId}${artifactId}" <=> "${o.groupId}${o.artifactId}"
		}
	}

	static class Versions {
		def snapshot
		def release
	}

	// HTMLスクレイピング用パターン
	static final def DIR_PATTERN = ~/[^?\/]+\//
	static final def POM_PATTERN = ~/.+\.pom/
	static final def METADATA_PATTERN = ~/maven-metadata\.xml/

	def writers

	def artifactMap = [:].withDefault{ new Versions() }

	public static void main(String[] args) {
		new RepoArtifactExtractor().execute(args)
	}

	def execute(args) {
		// TODO そろそろ引数が多くなってきたので、プロパティファイルに切り出したほうがいい
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
		artifactMap.sort{ it.key }.each { artifact, versions ->
			// TODO ハイパーリンク用URLを組み立てる
			writers.each {
				it.write(artifact.groupId, artifact.artifactId, versions.snapshot, " url ")
			}
		}
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
			def metadataUrl = "${url}${getMetadata(url).@href.toString()}"
			def latest = getLatestVersion(metadataUrl)
			findPom("${url}${latest}/")
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
	 * メタデータを取得する
	 */
	def getMetadata(url) {
		def parser = new XmlSlurper(new SAXParser())
		def html = parser.parse(url)

		return html.'**'.find {
			it.name() == 'A' && METADATA_PATTERN.matcher(it.@href.toString()).matches()
		}
	}

	/**
	 * 最新バージョンを取得する
	 */
	def getLatestVersion(metadataUrl) {
		def metadata = new XmlSlurper().parse(metadataUrl)

		if (metadata.versioning.latest.isEmpty() == false) {
			return metadata.versioning.latest.toString()
		} else {
			return metadata.versioning.versions.version[-1].toString()
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

		// TODO SNAPSHOTとRELEASEに応じて、書込み先のプロパティを切り替える
		def artifact = new Artifact()
		artifact.groupId = groupId
		artifact.artifactId = artifactId
		def versions = artifactMap[artifact]
		versions.snapshot = version
		artifactMap[artifact] = versions
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