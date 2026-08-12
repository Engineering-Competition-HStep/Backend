package com.Hstep.Hstep.domain.track.controller;

import com.Hstep.Hstep.domain.track.dto.TrackDto;
import com.Hstep.Hstep.domain.track.exception.TrackResponseCode;
import com.Hstep.Hstep.domain.track.service.TrackService;
import com.Hstep.Hstep.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    // 회원가입 화면 등 비로그인 상태에서도 트랙 목록을 볼 수 있어야 하므로 인증 없이 공개 API로 노출한다.
    @GetMapping
    public SuccessResponse<List<TrackDto.Response>> findAll() {
        return SuccessResponse.of(trackService.findAll(), TrackResponseCode.TRACK_LIST_GET_SUCCESS);
    }
}
