package org.ikikko.writer



interface ArtifactWriter {

	def init()

	def write(Artifact, Versions, url)

	def close()
}
