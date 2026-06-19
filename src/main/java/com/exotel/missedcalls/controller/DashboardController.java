package com.exotel.missedcalls.controller;

import com.exotel.missedcalls.dto.MissedCallResponse;
import com.exotel.missedcalls.dto.PagedResponse;
import com.exotel.missedcalls.service.MissedCallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Serves the server-rendered Thymeleaf dashboard for viewing missed calls.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MissedCallService missedCallService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("missedCallTime").descending());

        PagedResponse<MissedCallResponse> result = missedCallService.search(phoneNumber, fromDate, toDate, pageable);

        model.addAttribute("missedCalls", result.getContent());
        model.addAttribute("currentPage", result.getPageNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("phoneNumber", phoneNumber);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        model.addAttribute("totalCount", missedCallService.getTotalCount());
        model.addAttribute("todayCount", missedCallService.getTodayCount());
        model.addAttribute("latestCalls", missedCallService.getLatest());
        model.addAttribute("routeSummary", missedCallService.getMissedCallAggregates());

        return "dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
