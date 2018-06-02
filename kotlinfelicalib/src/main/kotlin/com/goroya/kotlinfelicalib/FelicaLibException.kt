package com.goroya.kotlinfelicalib

class FelicaLibException : Exception {
    constructor(msg: String) : super(msg)
    constructor(exception: Exception) : super(exception)
}
