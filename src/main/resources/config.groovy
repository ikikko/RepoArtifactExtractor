repository {
	release {
		url = 'http://maven.seasar.org/maven2/org/seasar/cubby/'
		type = 'release'
	}

	snapshot {
		url = 'http://maven.seasar.org/maven2-snapshot/org/seasar/cubby/'
		type = 'snapshot'
	}

	thirdparty {
		url = 'http://maven.seasar.org/maven2/org/seasar/mayaa/'
		type = 'release'
	}
}

hyperlink { baseUrl = 'http://maven.seasar.org/maven2/org/seasar/cubby/' }

writer {
	console {
		clazz = org.ikikko.writer.ConsoleArtifactWriter.class
	}

	excel {
		clazz = org.ikikko.writer.ExcelArtifactWriter.class
		args = 'build/artifact.xls'
	}

	//	trac {
	//		clazz = org.ikikko.writer.TracArtifactWriter.class
	//		args = 'http://localhost:8080/trac/'
	//	}
}
