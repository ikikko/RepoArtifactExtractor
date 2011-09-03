url {
	release = 'http://maven.seasar.org/maven2/org/seasar/cubby/'
	snapshot = 'http://maven.seasar.org/maven2/org/seasar/cubby/'
	thirdparty = 'http://maven.seasar.org/maven2/org/seasar/cubby/'

	virtual = 'http://maven.seasar.org/maven2/org/seasar/cubby/'
}

writer {
	console {
		clazz = org.ikikko.writer.ConsoleArtifactWriter.class
	}

	excel {
		clazz = org.ikikko.writer.ExcelArtifactWriter.class
		args = 'bin/artifact.xls'
	}

	/*
	 trac {
	 clazz = org.ikikko.writer.TracArtifactWriter.class
	 args = 'http://localhost:8080/trac/'
	 }
	 */
}
