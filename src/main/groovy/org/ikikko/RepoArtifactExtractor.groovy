package org.ikikko

import groovy.transform.EqualsAndHashCode

import org.cyberneko.html.parsers.SAXParser

class RepoArtifactExtractor {

	// TODO HTMLスクレイピングではなくて、別の方法も検討する（例：Artifactory REST API）

	/**
	 * アーティファクトを一意に特定するクラス
	 */
	@EqualsAndHashCode
	static class Artifact implements Comparable {
		def groupId
		def artifactId

		@Override
		public int compareTo(def o) {
			"${groupId}${artifactId}" <=> "${o.groupId}${o.artifactId}"
		}
	}

	/**
	 * 各々のバージョンを特定するクラス
	 */
	static class Versions {
		def snapshot
		def release
	}

	// HTMLスクレイピング用パターン
	static final def DIR_PATTERN = ~/[^?\/]+\//
	static final def POM_PATTERN = ~/.+\.pom/
	static final def METADATA_PATTERN = ~/maven-metadata\.xml/

	def config = new ConfigSlurper().parse(RepoArtifactExtractor.class.getResource('/config.groovy'))

	def artifactMap = [:].withDefault{ new Versions() }

	def currentReposId

	public static void main(String[] args) {
		new RepoArtifactExtractor().execute()
	}

	def execute() {
		def writers = config.writer.collect { id, writer ->
			writer.clazz.newInstance(writer.args)
		}

		writers.each { it.init() }

		config.repository.each { id, repos ->
			currentReposId = id
			traverseDir(repos.url)
		}

		artifactMap.sort{ it.key }.each { artifact, versions ->
			writers.each {
				def url = createLinkToArtifact(artifact, config.hyperlink.baseUrl)
				it.write(artifact, versions, url)
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

		def first = links[0]
		if (first == null) {
			return
		}

		// バージョン階層のディレクトリ(pom.xmlがあるディレクトリ)までトラバースしたら、最新のバージョンのみ対象とする
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

		def groupId = pom.groupId.isEmpty() ? pom.parent.groupId.toString() : pom.groupId.toString()
		def artifactId = pom.artifactId.toString()
		def version = pom.version.isEmpty() ? pom.parent.version.toString() : pom.version.toString()

		def repositoryType = config.repository."${currentReposId}".type

		def artifact = new Artifact()
		artifact.groupId = groupId
		artifact.artifactId = artifactId
		def versions = artifactMap[artifact]
		versions."${repositoryType}" = version
		artifactMap[artifact] = versions
	}

	/**
	 * アーティファクトへのリンクを構築する
	 */
	def createLinkToArtifact(artifact, baseUrl) {
		def groupIdUrl = artifact.groupId.tr('.', '/')
		def artifactIdUrl = artifact.artifactId.tr('.', '/')

		return "$baseUrl$groupIdUrl/$artifactIdUrl/"
	}

}