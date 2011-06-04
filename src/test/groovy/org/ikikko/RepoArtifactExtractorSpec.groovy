package org.ikikko

import org.apache.poi.hssf.record.formula.functions.T

import spock.lang.Specification

class RepoArtifactExtractorSpec extends Specification {

	def extractor = new RepoArtifactExtractor()

	def "アーティファクトを抽出する"() {
		when:
		extractor.execute([
			'bin/artifact.xls',
			'http://maven.seasar.org/maven2/org/seasar/cubby/'
		]
		as String[])

		then:
		true
	}
}
