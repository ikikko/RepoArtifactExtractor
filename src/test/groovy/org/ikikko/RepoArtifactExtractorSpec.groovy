package org.ikikko

import org.apache.poi.hssf.record.formula.functions.T

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

	def "POMのURLからアーティファクトのURLを抽出する"() {
		setup:
		def pomUrl = 'http://maven.seasar.org/maven2/org/seasar/cubby/cubby-unit/2.0.9/cubby-unit-2.0.9.pom'
		def expectUrl = 'http://maven.seasar.org/maven2/org/seasar/cubby/cubby-unit/'

		expect:
		extractor.extractArtifactUrl(pomUrl) == expectUrl
	}

	def "最新バージョンを取得する"() {
		setup:
		def url = 'http://maven.seasar.org/maven2/org/seasar/cubby/cubby-unit/maven-metadata.xml'

		expect:
		extractor.getLatestVersion(url) == '2.0.9'
	}
}
