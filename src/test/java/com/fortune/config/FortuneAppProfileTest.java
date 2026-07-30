package com.fortune.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneAppProfileTest {

    @Test
    void separatesRequiredProfileFromOptionalNotifications() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("1. 공통 사용자 정보")
                .contains("2. 공통 알림 발송 정보")
                .contains("<legend>출생 도시와 위치</legend>")
                .contains("id=\"profile-birth-city\"")
                .contains(".profile-field-group .form-row { grid-template-columns:repeat(4,minmax(0,1fr));")
                .contains("@media (max-width: 640px)")
                .contains(".profile-field-group .form-row { grid-template-columns:1fr 1fr; }")
                .contains(".form-group { display: flex; flex-direction: column; gap: 0.4rem; min-width:0; }")
                .contains("width:100%; height:43px; min-height:43px; min-width:0;")
                .contains("설정을 적용하는 것만으로는 발송되지 않습니다")
                .contains("현재 알림 미사용");
    }

    @Test
    void validatesSelectedNotificationChannelsWhenApplyingSettings() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("buildCommonProfile(true);")
                .contains("['email', 'both', 'all'].includes(type)")
                .contains("['telegram', 'both', 'all'].includes(type)")
                .contains("/^-?[0-9]+$/.test(telegramChatId)")
                .contains("maxlength=\"254\"")
                .contains("updateNotificationSyntax(this)")
                .contains("Telegram Bot Token")
                .contains("form-group form-full notif-channel-field\" data-channel=\"telegram\"")
                .contains("서버 OpenBao 보안 설정 사용")
                .contains("id=\"profile-discord-target\"")
                .contains("loadDiscordWebhookConfig();")
                .contains("discordWebhookTarget: discordWebhookUrl ? null : discordWebhookTarget")
                .contains("localStorage.setItem(DISCORD_TARGET_STORAGE_KEY, target)")
                .contains("직접 입력한 URL은 세션 종료 후 저장하지 않습니다.");
    }

    @Test
    void validatesCalendarDateAndSupportsLunarLeapMonth() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("function updateCalendarFields()")
                .contains("실제 달력에 존재하는 출생일을 입력하세요.")
                .contains("출생일은 오늘 이후일 수 없습니다.")
                .contains("leapMonth:");
    }

    @Test
    void showsEveryTojeongMonthDetailInitially() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("const bestMonth = sortedMonths[0];")
                .contains("<details class=\"tojeong-month${isCurrent ? ' current' : ''}\" open>")
                .doesNotContain("const shouldOpen = isCurrent || month === bestMonth;");
    }

    @Test
    void explainsHowDailyScoreCombinesCalendarAndPersonalFactors() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("일진 기본점수")
                .contains("개인화 점수")
                .contains("d.scoreBasis");
    }

    @Test
    void rendersLuckyColorsAsAccessibleColorPickers() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("const LUCKY_COLOR_HEX")
                .contains("'빨간색':'#EF4444'")
                .contains("'아이보리':'#FFFFF0'")
                .contains("function renderLuckyColor")
                .contains("<input type=\"color\"")
                .contains("class=\"lucky-color-picker\"")
                .contains("색상 코드");
    }

    @Test
    void rendersAiMarkdownWithoutTrustingRawHtml() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("function renderAiMarkdown(markdown)")
                .contains("function renderAiInlineMarkdown(value)")
                .contains("AI 출력의 HTML은 먼저 escape")
                .contains(".replace(/\\*\\*([^*\\n]+)\\*\\*/g, '<strong>$1</strong>')")
                .contains("class=\"ai-markdown\"")
                .doesNotContain("`상세 ${index + 1}`");
    }

    @Test
    void submitsAndRendersPersonalizedWesternAstrology() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("<span class=\"tab-icon\" aria-hidden=\"true\">⭐</span> 점성술")
                .contains("<div class=\"card-title\">⭐ 서양 점성술</div>")
                .contains("id=\"profile-birth-latitude\"")
                .contains("id=\"profile-birth-longitude\"")
                .contains("id=\"profile-birth-timezone\"")
                .contains("function buildBirthLocation()")
                .contains("function buildAstrologyRequest(profile, targetDate, notification = null)")
                .contains("birthTime:")
                .contains("calendarType: profile.calendarType")
                .contains("☉ 개인 출생 차트")
                .contains("🪐 대상일 트랜짓")
                .contains("astrologyProfile")
                .contains("majorTransits")
                .contains("orb ");
    }

    @Test
    void addsDailyWeeklyAndAnnualAstrologyWithoutDuplicatingNotifications() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("Promise.all([dailyRequest, astrologyRequest])")
                .contains("apiPost('/api/fortune/zodiac', astrologyBody)")
                .contains("function renderAstrologyToday(d)")
                .contains("⭐ 오늘의 점성술")
                .contains("function renderAstrologyWeek(weekly, targetDate)")
                .contains("📅 주간 운세")
                .contains("주간 점수는 어떻게 산출되나요?")
                .contains("function renderAstrologyYear(annual)")
                .contains("년 연간 운세")
                .contains("12개월 흐름과 활용·점검 시기")
                .contains("연간 점수는 어떻게 산출되나요?")
                .contains("function localDateValue(date = new Date())");
    }

    @Test
    void explainsAstrologyReferenceDateAndLabelsDailyResultBySelectedDate() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("<label for=\"zodiac-target-date\">운세 기준 날짜</label>")
                .contains("선택한 날짜를 기준으로 일일·주간·월간·연간 운세를 계산합니다.")
                .contains("aria-describedby=\"zodiac-target-help\"")
                .contains("onclick=\"setZodiacTargetToday()\"")
                .contains(">오늘로 이동</button>")
                .contains("function formatAstrologyTargetDate(dateValue)")
                .contains("const isToday = targetDate === localDateValue();")
                .contains("isToday ? '☀️ 오늘의 점성술'")
                .contains("`☀️ ${targetDateLabel} 점성술 운세`")
                .doesNotContain("<label for=\"zodiac-target-date\">운세를 볼 날짜</label>");
    }

    @Test
    void searchesAndAppliesBirthLocationFromCommonProfile() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("onsubmit=\"searchBirthLocation(); return false;\"")
                .contains("/api/location/search?q=")
                .contains("function selectBirthLocation(index)")
                .contains("profile-birth-city")
                .contains("profile-birth-latitude")
                .contains("profile-birth-longitude")
                .contains("profile-birth-timezone")
                .doesNotContain("id=\"zodiac-location-query\"");
    }

    private String loadFortuneApp() throws IOException {
        return new ClassPathResource("static/fortune-app.html")
                .getContentAsString(StandardCharsets.UTF_8);
    }
}
