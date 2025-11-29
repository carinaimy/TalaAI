package com.tala.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Interest scores response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestScoresResponse {
    public Long userId;
    public Long profileId;
    public Map<String, Double> interestVector;
    public List<String> explicitTopics;
    public List<String> recentTopics;
}
