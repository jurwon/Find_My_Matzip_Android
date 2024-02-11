package com.matzip.find_my_matzip.model

import java.security.Timestamp

data class TempChat (
    var id: String? = null,
    var user: String? = null,
    var meg: String? = null,
    var crateAt: Timestamp? = null,
)