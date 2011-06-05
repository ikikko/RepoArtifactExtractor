package org.ikikko.writer

import groovy.net.xmlrpc.XMLRPCServerProxy

class TracArtifactWriter implements ArtifactWriter {

	static def separator = System.properties['line.separator']

	def url
	def proxy

	def content = new StringBuilder()

	TracArtifactWriter(url) {
		this.url = url
	}

	@Override
	def init() {
		proxy = new XMLRPCServerProxy("${url}rpc")
		content << "|| '''groupId''' || '''artifactId''' || '''version''' || '''Ivy Depencency''' ||$separator"
	}

	@Override
	def write(groupId, artifactId, version, url) {
		def ivy = "<dependency org=\"$groupId\" name=\"$artifactId\" rev=\"$version\" />"
		content << "|| $groupId || [$url $artifactId] || $version || $ivy ||$separator"
	}

	@Override
	def close() {
		proxy.wiki.putPage('RepoArtifactExtractor', content.toString(), [:])
	}
}
