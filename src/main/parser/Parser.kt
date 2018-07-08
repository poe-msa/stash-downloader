package parser

import com.beust.klaxon.Klaxon
import parser.result.ParserResult

class Parser {
    fun parse(data: String): ParserResult {
        val parseBeforeMs = System.currentTimeMillis()
        val stashResult = Klaxon().parse<ParsedStashResult>(data)
        val parseAfterMs = System.currentTimeMillis()
        val totalParseMs = (parseAfterMs - parseBeforeMs).toInt()

        return ParserResult(stashResult!!.nextChangeId, data, totalParseMs)
    }
}