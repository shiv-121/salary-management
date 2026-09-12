package com.acme.salary.controller;

import com.acme.salary.dto.analytics.CompensationBreakdownResponse;
import com.acme.salary.dto.analytics.CompensationSummaryResponse;
import com.acme.salary.service.CompensationAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
                    "All salary values are converted to USD using static assessment rates. " +
                    "Employees without a current salary remain included in totalEmployees."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CompensationSummaryResponse.class)))
    })
    public ResponseEntity<CompensationSummaryResponse> getSummary() {
        return ResponseEntity.ok(compensationAnalyticsService.getSummary());
    }

    @GetMapping("/by-country")
    @Operation(
            summary = "Get compensation by country",
            description = "Returns grouped salary metrics by employee country using current salaries only and USD conversion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Country breakdown retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CompensationBreakdownResponse.class)))
    })
    public ResponseEntity<List<CompensationBreakdownResponse>> getByCountry() {
        return ResponseEntity.ok(compensationAnalyticsService.getByCountry());
    }

    @GetMapping("/by-department")
    @Operation(
            summary = "Get compensation by department",
            description = "Returns grouped salary metrics by department using current salaries only and USD conversion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department breakdown retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CompensationBreakdownResponse.class)))
    })
    public ResponseEntity<List<CompensationBreakdownResponse>> getByDepartment() {
        return ResponseEntity.ok(compensationAnalyticsService.getByDepartment());
    }

    @GetMapping("/by-job-title")
    @Operation(
            summary = "Get compensation by job title",
            description = "Returns grouped salary metrics by job title using current salaries only and USD conversion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job title breakdown retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CompensationBreakdownResponse.class)))
    })
    public ResponseEntity<List<CompensationBreakdownResponse>> getByJobTitle() {
        return ResponseEntity.ok(compensationAnalyticsService.getByJobTitle());
    }
}
