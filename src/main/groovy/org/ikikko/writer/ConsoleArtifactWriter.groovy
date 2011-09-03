package org.ikikko.writer

class ConsoleArtifactWriter implements ArtifactWriter {

	@Override
	def init() {
	}

	@Override
	def write(artifact, versions, url) {
		println "[groupId : ${artifact.groupId}], [artifactId : ${artifact.artifactId}], [release : ${versions.release}], [snapshot : ${versions.snapshot}]"
	}

	@Override
	def close() {
	}
}
