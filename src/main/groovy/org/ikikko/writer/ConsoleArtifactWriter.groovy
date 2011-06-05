package org.ikikko.writer

class ConsoleArtifactWriter implements ArtifactWriter {

	@Override
	def init() {
	}

	@Override
	def write(groupId, artifactId, version, url) {
		println "groupId : $groupId, artifactId : $artifactId, version : $version"
	}

	@Override
	def close() {
	}
}
