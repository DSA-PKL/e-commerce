package com.dsapkl.backend.util;

import com.dsapkl.backend.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    //다른 컨트롤러에서도 사용하기 위해 static
    public static Member getMember(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        //비로그인 사용자
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return null;
        }

        //세션에 저장되어있는 회원정보 가져오기
        Member member = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return member;
    }

}
