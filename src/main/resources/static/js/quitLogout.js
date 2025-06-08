// quitLogout.js

// 1) 초기 로드 시점: “페이지가 정상적으로 로드된 이후”에는 
//    unload 이벤트가 바로 로그아웃을 보내지 않도록 막기 위해
let isNavigatingAway = true; // 로드 직후에는 탭 클로즈 여부 검사 끄기

window.addEventListener("DOMContentLoaded", () => {
  // 이제부터는 “진짜 이동/클릭”만 탐지
  isNavigatingAway = false;

  // 2) 내부 링크(a 태그) 클릭 시: 이동 플래그 세팅
  document.querySelectorAll("a").forEach(a => {
    a.addEventListener("click", () => {
      isNavigatingAway = true;
    });
  });

  // 3) 로그인/로그아웃 버튼 클릭 시: 
  //    (로그아웃 버튼은 실제 POST/GET 방식으로 로그아웃을 처리하므로, 여기서는 단순히 플래그만 세팅)
  const logoutButton = document.querySelector('a[href="/logout]');
  if (logoutButton) {
    logoutButton.addEventListener("click", () => {
      isNavigatingAway = true;
    });
  }

  // 4) F5, Ctrl+R 등 리로드 단축키 탐지
  window.addEventListener("keydown", function(e) {
    if (e.key === "F5" || (e.ctrlKey && e.key.toLowerCase() === "r")) {
      isNavigatingAway = true;
    }
  });
});

// 5) “탭이나 창을 실제로 닫았을 때”만 로그아웃 요청을 보냄
window.addEventListener("beforeunload", function(e) {
  if (!isNavigatingAway) {
    // 백그라운드 비콘으로 로그아웃 호출
    navigator.sendBeacon("/logout");
    // ※ 네비게이션이나 리다이렉트 혹은 버튼 클릭은 isNavigatingAway=true로 막아서 이 구문이 실행되지 않습니다.
  }
});
