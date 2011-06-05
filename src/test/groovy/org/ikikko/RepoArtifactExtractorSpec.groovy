package org.ikikko

import org.apache.poi.hssf.record.formula.functions.T

import spock.lang.Specification

class RepoArtifactExtractorSpec extends Specification {

	def extractor = new RepoArtifactExtractor()

	def "アーティファクトを抽出する"() {
		setup:
		def excel = 'bin/artifact.xls'
		def baseUrl = 'http://maven.seasar.org/maven2/org/seasar/cubby/'
		def tracUrl = 'http://localhost:8080/trac/'
		new File(excel).delete()

		when:
		extractor.execute([excel, baseUrl, tracUrl]as String[])

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
}
