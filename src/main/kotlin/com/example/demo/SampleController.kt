package com.example.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class SampleController(
        private val sampleService: SampleService
) {

    @GetMapping("/getData")
    fun getData(): Any {
        return sampleService.getData()
    }

    @PostMapping("/saveData")
    fun saveData(): Any {
        return sampleService.saveData(
                TbUser(
                        null,
                        "morph's id",
                        "morph's name",
                        "morph@email",
                        1,
                        "N",
                        Date(),
                        Date()
                )
        )
    }

}