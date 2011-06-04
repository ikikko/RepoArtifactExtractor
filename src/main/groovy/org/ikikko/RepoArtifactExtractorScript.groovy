package org.ikikko

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Hyperlink
import org.apache.poi.ss.usermodel.IndexedColors
import org.cyberneko.html.parsers.SAXParser

// 実行方法
def usage = '''
Usage   : groovy RepoArtifactExtractorScript.groovy '\${excel file}' '\${extract url}
Example : groovy RepoArtifactExtractorScript.groovy artifacts.xls http://maven.seasar.org/maven2/org/seasar/cubby/
'''

// HTMLスクレイピング用パターン
dirPattern = ~/[^?\/]+\//
pomPattern = ~/.+\.pom/

// Excel用変数
book = null
sheet = null
i = 0

// 引数セット
if (args.length != 2) {
	System.err.println usage
	System.exit 1
}
excel = args[0]
baseUrl = args[1]

// Main
createExcel()
traverseDir(baseUrl)
writeExcel()

/**
 * ディレクトリをトラバースする
 */
def traverseDir(url) {
	def parser = new XmlSlurper(new SAXParser())
	def html = parser.parse(url)

	def links = html.'**'.findAll{
		// トラバーサルロジックは色々変わるかも
		it.name() == 'A' && dirPattern.matcher(it.@href.toString()).matches()
	}

	// バージョン階層のディレクトリまでトラバースしたら、最新のバージョンのみ対象とする
	def first = links[0]
	if (isPomExistsDir("${url}${first.@href}")) {
		def latest = links[-1]
		findPom("${url}${latest.@href}")
	} else {
		links.each { traverseDir("${url}${it.@href}") }
	}
}

/**
 * POMが存在するディレクトリか否かを判定する
 */
def isPomExistsDir(url) {
	def parser = new XmlSlurper(new SAXParser())
	def html = parser.parse(url)

	return html.'**'.any {
		it.name() == 'A' && pomPattern.matcher(it.@href.toString()).matches()
	}
}

/**
 * POMを検索する
 */
def findPom(url) {
	def parser = new XmlSlurper(new SAXParser())
	def html = parser.parse(url)

	def pom = html.'**'.find {
		it.name() == 'A' && pomPattern.matcher(it.@href.toString()).matches()
	}
	parsePom("${url}${pom.@href}")
}

/**
 * POMをパースする
 */
def parsePom(url) {
	def pom = new XmlSlurper().parse(url);

	def groupId = pom.groupId.isEmpty() ? pom.parent.groupId : pom.groupId
	def artifactId = pom.artifactId
	def version = pom.version.isEmpty() ? pom.parent.version : pom.version

	createArtifactRow(url, groupId, artifactId, version)

	println "groupId : $groupId, artifactId : $artifactId, version : $version"
}

/**
 * Excelファイルを作成する
 */
def createExcel() {
	book = new HSSFWorkbook()
	sheet = book.createSheet('artifacts')

	createHeader()
}

/**
 * Excelファイルのヘッダ行を作成する
 */
def createHeader() {
	cell(i, 0).setCellValue('Group ID')
	cell(i, 1).setCellValue('Artifact ID')
	cell(i, 2).setCellValue('Version')
	cell(i, 3).setCellValue('Ivy Dependency')

	def font = book.createFont()
	font.setBoldweight(Font.BOLDWEIGHT_BOLD)
	def style = book.createCellStyle()
	style.setFont(font)
	style.setAlignment(CellStyle.ALIGN_CENTER);
	for (j in 0..3) {
		cell(i, j).setCellStyle(style)
	}

	i++
}

/**
 * アーティファクトの行を作成する
 */
def createArtifactRow(url, groupId, artifactId, version) {
	def ivy = "<dependency org=\"$groupId\" name=\"$artifactId\" rev=\"$version\" />"

	cell(i, 0).setCellValue(groupId.toString())
	cell(i, 1).setCellValue(artifactId.toString())
	cell(i, 2).setCellValue(version.toString())
	cell(i, 3).setCellValue(ivy.toString())

	def paths = url.tokenize('/')
	def artifactUrl = (url - paths[-1] - paths[-2])[0..-2]
	def link = book.getCreationHelper().createHyperlink(Hyperlink.LINK_URL)
	link.setAddress(artifactUrl)
	cell(i, 1).setHyperlink(link)

	def font = book.createFont()
	font.setUnderline(Font.U_SINGLE)
	font.setColor(IndexedColors.BLUE.getIndex())
	def style = book.createCellStyle()
	style.setFont(font)
	cell(i, 1).setCellStyle(style)

	i++
}

/**
 * Excelファイルに書きこむ
 */
def writeExcel() {
	for (j in 0..3) {
		sheet.autoSizeColumn(j)
	}

	new File(excel).withOutputStream { out ->
		book.write(out)
	}
}

/**
 * セルへのショートカット
 */
def cell(i, j) {
	def row = sheet.getRow(i) ? sheet.getRow(i) : sheet.createRow(i)
	def cell = row.getCell(j) ? row.getCell(j) : row.createCell(j)

	return cell
}
