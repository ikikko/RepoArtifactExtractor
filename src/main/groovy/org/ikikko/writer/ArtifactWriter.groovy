package org.ikikko.writer

interface ArtifactWriter {

	def init()

	def write(groupId, artifactId, version, url)

	def close()
}
