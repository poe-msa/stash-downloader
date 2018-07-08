package parser

import com.beust.klaxon.Json

data class ParsedStashResult(
    @Json(name = "next_change_id")
    val nextChangeId: String
) {

}