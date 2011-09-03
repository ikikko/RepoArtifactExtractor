package org.ikikko.writer

import org.apache.poi.common.usermodel.Hyperlink
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors

class ExcelArtifactWriter implements ArtifactWriter {

	static final def columns = [
		'Group ID',
		'Artifact ID',
		'Release Version',
		'Snapshot Version',
		'Ivy Dependency',
	]

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
	def write(artifact, versions, url) {
		createArtifactRow(artifact, versions, url)
	}

	@Override
	def close() {
		for (int i = 0; i < columns.size(); i++) {
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
		for (int i = 0; i < columns.size(); i++) {
			cell(rowIndex, i).setCellValue(columns[i])
		}

		def font = book.createFont()
		font.setBoldweight(Font.BOLDWEIGHT_BOLD)
		def style = book.createCellStyle()
		style.setFont(font)
		style.setAlignment(CellStyle.ALIGN_CENTER);
		for (int i = 0; i < columns.size(); i++) {
			cell(rowIndex, i).setCellStyle(style)
		}

		rowIndex++
	}

	/**
	 * アーティファクトの行を作成する
	 */
	def createArtifactRow(artifact,  versions, url) {
		def ivy = "<dependency org=\"${artifact.groupId}\" name=\"${artifact.artifactId}\" rev=\"${versions.release}\" />"

		cell(rowIndex, 0).setCellValue(artifact.groupId.toString())
		cell(rowIndex, 1).setCellValue(artifact.artifactId.toString())
		cell(rowIndex, 2).setCellValue(versions.release.toString())
		cell(rowIndex, 3).setCellValue(versions.snapshot.toString())
		cell(rowIndex, 4).setCellValue(ivy.toString())


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
