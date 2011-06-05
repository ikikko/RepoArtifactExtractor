package org.ikikko.writer

import org.apache.poi.common.usermodel.Hyperlink
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors

class ExcelArtifactWriter implements ArtifactWriter {

	// Excel用フィールド
	def excel
	def book
	def sheet
	def rowIndex = 0

	ExcelArtifactWriter(excel) {
		this.excel = excel
	}

	@Override
	def init() {
		book = new HSSFWorkbook()
		sheet = book.createSheet('artifacts')

		createHeader()
	}

	@Override
	def write(groupId, artifactId, version, url) {
		createArtifactRow(groupId, artifactId, version, url)
	}

	@Override
	def close() {
		for (i in 0..3) {
			sheet.autoSizeColumn(i)
		}

		new File(excel).withOutputStream { out ->
			book.write(out)
		}
	}

	/**
	 * Excelファイルのヘッダ行を作成する
	 */
	def createHeader() {
		cell(rowIndex, 0).setCellValue('Group ID')
		cell(rowIndex, 1).setCellValue('Artifact ID')
		cell(rowIndex, 2).setCellValue('Version')
		cell(rowIndex, 3).setCellValue('Ivy Dependency')

		def font = book.createFont()
		font.setBoldweight(Font.BOLDWEIGHT_BOLD)
		def style = book.createCellStyle()
		style.setFont(font)
		style.setAlignment(CellStyle.ALIGN_CENTER);
		for (i in 0..3) {
			cell(rowIndex, i).setCellStyle(style)
		}

		rowIndex++
	}

	/**
	 * アーティファクトの行を作成する
	 */
	def createArtifactRow(groupId, artifactId, version, url) {
		def ivy = "<dependency org=\"$groupId\" name=\"$artifactId\" rev=\"$version\" />"

		cell(rowIndex, 0).setCellValue(groupId.toString())
		cell(rowIndex, 1).setCellValue(artifactId.toString())
		cell(rowIndex, 2).setCellValue(version.toString())
		cell(rowIndex, 3).setCellValue(ivy.toString())

		def link = book.getCreationHelper().createHyperlink(Hyperlink.LINK_URL)
		link.setAddress(url)
		cell(rowIndex, 1).setHyperlink(link)

		def font = book.createFont()
		font.setUnderline(Font.U_SINGLE)
		font.setColor(IndexedColors.BLUE.getIndex())
		def style = book.createCellStyle()
		style.setFont(font)
		cell(rowIndex, 1).setCellStyle(style)

		rowIndex++
	}

	/**
	 * セルへのショートカット
	 */
	def cell(i, j) {
		def row = sheet.getRow(i) ? sheet.getRow(i) : sheet.createRow(i)
		def cell = row.getCell(j) ? row.getCell(j) : row.createCell(j)

		return cell
	}

}
