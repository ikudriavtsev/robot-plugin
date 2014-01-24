package parser

import com.intellij.lang.{ASTNode, PsiBuilder, PsiParser}
import com.intellij.psi.tree.IElementType
import amailp.elements.{RobotTokenTypes, RobotASTTypes}
import RobotASTTypes._
import RobotTokenTypes._

object RobotParser extends PsiParser {
  def parse(root: IElementType, builder: PsiBuilder): ASTNode = {
    import builder._
    def currentType = getTokenType

    def parseTable() = {
      val tableMarker = mark
      parseHeaderRow()
      while(hasMoreTokens && !isHeader(currentType))
        parseBodyRow()
      tableMarker.done(Table)
    }

    def parseHeaderRow() = {
      val headerMark = mark
      val headerType = currentType
      if(!isHeader(headerType))
        error("Header token expected")
      advanceLexer()
      headerMark.done(headerType)

      skipSpace()
      consumeLineTerminator()
    }
    
    def parseBodyRow() = {
      val rowMarker = mark
      while(!isRowTerminator(currentType))
      {
        skipSpace()
        parseCell()
      }
      consumeLineTerminator()
      rowMarker.done(TableRow)
    }

    def parseCell() = {
      val cellMarker = mark
      while(!isRowTerminator(currentType) && currentType != Whitespaces)
        advanceLexer()
      cellMarker.done(NonEmptyCell)
    }

    def hasMoreTokens = !eof

    def skipSpace() = if (currentType == Whitespaces || currentType == Space)
      advanceLexer()

    def consumeLineTerminator() = {
      if (!isRowTerminator(currentType))
        error("Line terminator expected")
      advanceLexer()
    }
    
    def isRowTerminator(token: IElementType) = token == LineTerminator || eof
    
    def isHeader(token: IElementType) = HeaderTokens contains token

    val rootMarker = mark
    while (hasMoreTokens) {
      parseTable()
    }
    rootMarker.done(root)
    builder.getTreeBuilt
  }
}