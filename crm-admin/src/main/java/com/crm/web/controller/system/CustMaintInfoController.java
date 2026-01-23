package com.crm.web.controller.system;

import com.crm.common.core.controller.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/custMaintInfo")
public class CustMaintInfoController extends BaseController {

}
