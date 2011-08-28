package org.ikikko.writer

import groovy.net.xmlrpc.XMLRPCServerProxy

import org.codehaus.groovy.runtime.EncodingGroovyMethods

class TracArtifactWriter implements ArtifactWriter {

	/**
	 * setBasicAuth()内でDeprecatedなメソッドを使用していてGroovy 1.8では動かないので、修正を加えています。
	 * 
	 * @see http://jira.codehaus.org/browse/GMOD-239
	 */
	class XMLRPCServerProxy18 extends XMLRPCServerProxy {

		XMLRPCServerProxy18(String paramString) {
			super(paramString)
		}

		@Override
		public void setBasicAuth(String paramString1, String paramString2)
		throws IOException {
			if (null == paramString1) {
				this.authString = null;
				return;
			}

			String str = paramString1 + ":" + paramString2;
			try {
				// this.authString = "Basic " + DefaultGroovyMethods.encodeBase64(str.getBytes("ISO-8859-1")).toString();
				this.authString = "Basic " + EncodingGroovyMethods.encodeBase64(str.getBytes("ISO-8859-1")).toString();
			}
			catch (UnsupportedEncodingException localUnsupportedEncodingException) {
				throw new IOException("Error encoding auth header: " + localUnsupportedEncodingException.getMessage());
			}
		}
	}

	static def separator = System.properties['line.separator']

	def url
	def proxy

	def content = new StringBuilder()

	TracArtifactWriter(url) {
		this.url = url
	}

	@Override
	def init() {
		proxy = new XMLRPCServerProxy18("${url}rpc")
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
