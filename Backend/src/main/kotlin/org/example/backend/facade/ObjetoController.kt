package org.example.backend.facade

import org.example.backend.service.ObjetoService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/objeto")
class ObjetoController (
    private val objetoService: ObjetoService,
){


}
