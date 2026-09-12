package com.acme.salary.controller;

import com.acme.salary.dto.analytics.CompensationBreakdownResponse;
import com.acme.salary.dto.analytics.CompensationSummaryResponse;
import com.acme.salary.enums.Currency;
import com.acme.salary.service.CompensationAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Compensation analytics and salary summaries")
public class AnalyticsController {

    private final CompensationAnalyticsService compensationAnalyticsService;

    public AnalyticsController(CompensationAnalyticsService compensationAnalyticsService) {
        this.compensationAnalyticsService = compensationAnalyticsService;
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get compensation summary",
            description = "Returns overall salary summary metrics for employees with a current salary. " +
                    "Current salaries only are included in salary statistics. Salary values are converted into the requested reporting currency using static assessment FX rates. " +
                    "USD is the default reporting currency. Employees without a current salary remain included in totalEmployees."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CompensationSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid reporting currency")
    })
    public ResponseEntity<CompensationSummaryResponse> getSummary(
            @Parameter(
                    description = "Reporting currency used to convert salary values for analytics. Defaults to USD. Supported values: USD, EUR, GBP, INR, CAD, AUD, SGD, JPY.",
                    example = "INR",
                    schema = @Schema(type = "string", allowableValues = {"USD", "EUR", "GBP", "INR", "CAD", "AUD", "SGD", "JPY"})
            )
            @RequestParam(defaultValue = "USD") Currency currency) {
        return ResponseEntity.ok(compensationAnalyticsService.getSummary(currency));
    }

    @GetMapping("/by-country")
    @Operation(
            summary = "Get compensation by country",
            description = "Returns grouped salary metrics by employee country using current salaries only and the selected reporting currency. " +
                    "The fixed assessment FX rates are used; they are not live market rates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Country breakdown retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CompensationBreakdownResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid reporting currency")
    })
    public ResponseEntity<List<CompensationBreakdownResponse>> getByCountry(
            @Parameter(
                    description = "Reporting currency for the breakdown. Defaults to USD. Supported values: USD, EUR, GBP, INR, CAD, AUD, SGD, JPY.",
                    example = "INR",
                    schema = @Schema(type = "string", allowableValues = {"USD", "EUR", "GBP", "INR", "CAD", "AUD", "SGD", "JPY"})
            )
            @RequestParam(defaultValue = "USD") Currency currency) {
        return ResponseEntity.ok(compensationAnalyticsService.getByCountry(currency));
    }

    @GetMapping("/by-department")
    @Operation(
            summary = "Get compensation by department",
            description = "Returns grouped salary metrics by department using current salaries only and the selected reporting currency. " +
                    "The fixed assessment FX rates are used; they are not live market rates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department breakdown retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CompensationBreakdownResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid reporting currency")
    })
    public ResponseEntity<List<CompensationBreakdownResponse>> getByDepartment(
            @Parameter(
                    description = "Reporting currency for the breakdown. Defaults to USD. Supported values: USD, EUR, GBP, INR, CAD, AUD, SGD, JPY.",
                    example = "INR",
                    schema = @Schema(type = "string", allowableValues = {"USD", "EUR", "GBP", "INR", "CAD", "AUD", "SGD", "JPY"})
            )
            @RequestParam(defaultValue = "USD") Currency currency) {
        return ResponseEntity.ok(compensationAnalyticsService.getByDepartment(currency));
    }

    @GetMapping("/by-job-title")
    @Operation(
            summary = "Get compensation by job title",
            description = "Returns grouped salary metrics by job title using current salaries only and the selected reporting currency. " +
                    "The fixed assessment FX rates are used; they are not live market rates."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job title breakdown retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CompensationBreakdownResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid reporting currency")
    })
    public ResponseEntity<List<CompensationBreakdownResponse>> getByJobTitle(
            @Parameter(
                    description = "Reporting currency for the breakdown. Defaults to USD. Supported values: USD, EUR, GBP, INR, CAD, AUD, SGD, JPY.",
                    example = "INR",
                    schema = @Schema(type = "string", allowableValues = {"USD", "EUR", "GBP", "INR", "CAD", "AUD", "SGD", "JPY"})
            )
            @RequestParam(defaultValue = "USD") Currency currency) {
        return ResponseEntity.ok(compensationAnalyticsService.getByJobTitle(currency));
    }
}
