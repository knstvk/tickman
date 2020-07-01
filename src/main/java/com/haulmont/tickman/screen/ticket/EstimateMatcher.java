package com.haulmont.tickman.screen.ticket;

import com.google.common.base.Splitter;
import com.haulmont.tickman.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EstimateMatcher {

    private static final Logger log = LoggerFactory.getLogger(EstimateMatcher.class);

    public static boolean matches(Ticket ticket, String filterExpr) {
        Integer estimate = ticket.getEstimate();
        List<String> filterParts = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(filterExpr);
        for (String filterPart : filterParts) {
            if (estimate == null && filterPart.equals("0")) {
                return true;
            }
            if (estimate != null) {
                try {
                    if (filterPart.startsWith(">=")) {
                        return estimate >= Integer.parseInt(filterPart.substring(2).trim());
                    } else if (filterPart.startsWith(">")) {
                        return estimate > Integer.parseInt(filterPart.substring(1).trim());
                    } else if (filterPart.startsWith("<=")) {
                        return estimate <= Integer.parseInt(filterPart.substring(2).trim());
                    } else if (filterPart.startsWith("<")) {
                        return estimate < Integer.parseInt(filterPart.substring(1).trim());
                    } else if (estimate == Integer.parseInt(filterPart)) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Unable to parse estimate filter expression {}: {}", filterExpr, e.toString());
                    return false;
                }
            }
        }
        return false;
    }
}
