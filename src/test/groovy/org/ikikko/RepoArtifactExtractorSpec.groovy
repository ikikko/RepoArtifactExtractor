package org.ikikko

import org.apache.poi.hssf.record.formula.functions.T
import org.ikikko.RepoArtifactExtractor.Artifact

import spock.lang.Specification

class RepoArtifactExtractorSpec extends Specification {

	def extractor = new RepoArtifactExtractor()

	def "アーティファクトを抽出する"() {
		setup:
		def excel = extractor.config.writer.excel.args
		new File(excel).delete()

		when:
		extractor.execute()

		then:
		new File(excel).exists()
	}

	def "最新バージョンを取得する"() {
		setup:
		def url = 'http://maven.seasar.org/maven2/org/seasar/cubby/cubby-unit/maven-metadata.xml'

		expect:
		extractor.getLatestVersion(url) == '2.0.9'
	}

	def "アーティファクトへのリンクを構築する"() {
		setup:
		def artifact = new Artifact()
		artifact.groupId = 'org.seasar.cubby'
		artifact.artifactId = 'cubby-unit'
		def baseUrl = 'http://maven.seasar.org/maven2/'

		when:
		def result = extractor.createLinkToArtifact(artifact, baseUrl)

		then:
		result == 'http://maven.seasar.org/maven2/org/seasar/cubby/cubby-unit/'
	}
}
