package com.fortune.service;

import com.fortune.dto.TojeongResult;
import com.fortune.entity.TojeongGwaEntity;
import com.fortune.repository.TojeongGwaRepository;
import com.fortune.exception.FortuneCalculationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 토정비결 서비스
 * 토정비결 계산 메인 메서드
 * 월별 상세 운세 생성
 * 계절별 운세 생성
 * 연간 종합 조언 생성
 * 길한 달 선별
 * 주의할 달 선별
 */
@Service
public class TojeongBigyeolService {

    // 토정비결 괘 정보 클래스
    @Data
    @AllArgsConstructor
    public static class TojeongGwa {
        private String name;           // 괘명
        private String symbol;         // 괘 기호
        private String summary;        // 한줄 요약
        private String detailedFortune; // 상세 해석
        private int overallScore;      // 종합 점수 (0-100)
    }

    // 토정비결 괘 정보 맵
    private static final Map<Integer, TojeongGwa> TOJEONG_GWA_MAP = new HashMap<>();

    static {
        // ===== 상상괘 (上上卦) - 대길 (85-100점) =====
        TOJEONG_GWA_MAP.put(0, new TojeongGwa("건위천", "☰☰", "하늘이 도우니 크게 길하다",
                "올해는 하늘의 도움을 받아 모든 일이 순조롭게 풀릴 것입니다. 적극적으로 도전하고 새로운 사업이나 계획을 시작하기에 최적의 해입니다. 특히 리더십을 발휘할 기회가 많고, 주변의 도움도 받을 수 있습니다. 건강도 좋고 재물운도 상승합니다.", 95));

        TOJEONG_GWA_MAP.put(1, new TojeongGwa("곤위지", "☷☷", "땅의 덕으로 차근차근 발전한다",
                "꾸준함과 인내심이 빛을 발하는 해입니다. 급하게 서두르지 말고 기초를 탄탄히 다지세요. 여성이나 어머니의 도움이 클 것입니다. 부동산이나 안정적인 투자에 좋은 시기이며, 가정의 화목도 증진됩니다.", 88));

        TOJEONG_GWA_MAP.put(2, new TojeongGwa("수뢰둔", "☵☳", "어려움 속에서도 희망이 보인다",
                "초반에는 어려움이 있지만 꾸준히 노력하면 좋은 결과가 있을 것입니다. 새로운 시작의 해로 인내심을 갖고 기다리세요. 학업이나 새로운 기술 습득에 좋은 시기입니다.", 75));

        TOJEONG_GWA_MAP.put(3, new TojeongGwa("산수몽", "☶☵", "배움을 통해 지혜를 얻는다",
                "학습과 수양에 좋은 해입니다. 선배나 스승의 가르침을 받아들이면 큰 발전이 있을 것입니다. 젊은 사람들에게는 특히 학업운이 상승하며, 새로운 지식이나 기술을 익히기 좋습니다.", 82));

        TOJEONG_GWA_MAP.put(4, new TojeongGwa("수천수", "☵☰", "기다림 후에 큰 성취가 온다",
                "참고 기다리면 반드시 좋은 결과가 있을 것입니다. 성급하게 서두르지 말고 때를 기다리세요. 상반기는 준비 기간이고 하반기부터 본격적인 성과가 나타날 것입니다.", 78));

        TOJEONG_GWA_MAP.put(5, new TojeongGwa("천수송", "☰☵", "분쟁이 있으나 결국 해결된다",
                "갈등이나 문제가 있을 수 있지만 지혜롭게 대처하면 해결됩니다. 법적 문제나 계약 관련해서는 신중하게 접근하세요. 중재자의 도움을 받으면 좋은 결과를 얻을 수 있습니다.", 70));

        TOJEONG_GWA_MAP.put(6, new TojeongGwa("지수사", "☷☵", "많은 사람이 도와준다",
                "주변 사람들의 도움과 협력을 얻을 수 있는 해입니다. 팀워크나 조직력이 중요한 시기로, 혼자보다는 여러 사람과 함께하는 일에서 성과를 거둘 것입니다. 인맥 관리에 신경 쓰세요.", 85));

        TOJEONG_GWA_MAP.put(7, new TojeongGwa("수지비", "☵☷", "서로 가까워지는 좋은 시기",
                "인간관계가 원활해지고 협력이 잘 이루어지는 해입니다. 결혼이나 파트너십에 좋은 시기이며, 사업상 좋은 파트너를 만날 수 있습니다. 외교적 능력이 빛을 발할 것입니다.", 87));

        // ===== 상괘 (上卦) - 길 (70-84점) =====
        TOJEONG_GWA_MAP.put(8, new TojeongGwa("풍천소축", "☴☰", "작은 것을 모아 큰 것을 이룬다",
                "작은 노력들이 쌓여서 큰 성과를 만드는 해입니다. 절약과 저축이 중요하며, 꾸준한 노력이 결실을 맺을 것입니다. 세부적인 것에 신경 쓰고 계획적으로 행동하세요.", 76));

        TOJEONG_GWA_MAP.put(9, new TojeongGwa("천택리", "☰☱", "예의와 질서를 지키면 형통하다",
                "질서와 예의를 지키면서 행동하면 좋은 결과를 얻을 것입니다. 사회적 지위가 향상되고 품격 있는 행동이 인정받을 것입니다. 전통이나 관습을 존중하는 자세가 중요합니다.", 80));

        TOJEONG_GWA_MAP.put(10, new TojeongGwa("지천태", "☷☰", "하늘과 땅이 화합하여 크게 형통하다",
                "모든 일이 순조롭게 풀리는 최고의 해입니다. 상하가 화합하고 모든 관계가 원만해집니다. 새로운 사업을 시작하거나 중요한 결정을 내리기에 최적의 시기입니다.", 95));

        TOJEONG_GWA_MAP.put(11, new TojeongGwa("천지비", "☰☷", "막힌 것이 뚫리기 시작한다",
                "막혔던 일들이 서서히 풀리기 시작하는 해입니다. 인내심을 갖고 기다리면 좋은 결과가 있을 것입니다. 아직은 조심스럽게 행동하되, 희망을 잃지 마세요.", 72));

        TOJEONG_GWA_MAP.put(12, new TojeongGwa("천화동인", "☰☲", "뜻을 같이하는 사람들과 함께 한다",
                "같은 목표를 가진 사람들과의 협력이 중요한 해입니다. 동료나 친구들과의 관계가 좋아지고, 함께하는 일에서 큰 성과를 거둘 것입니다. 네트워킹에 신경 쓰세요.", 83));

        TOJEONG_GWA_MAP.put(13, new TojeongGwa("화천대유", "☲☰", "큰 소유를 얻게 된다",
                "재물운이 크게 상승하는 해입니다. 투자나 사업에서 좋은 성과를 거둘 것이며, 큰 수입이나 재산을 얻을 가능성이 높습니다. 다만 겸손함을 잃지 않도록 주의하세요.", 90));

        TOJEONG_GWA_MAP.put(14, new TojeongGwa("지산겸", "☷☶", "겸손하면 모든 것이 형통하다",
                "겸손한 마음가짐으로 임하면 모든 일이 잘 풀리는 해입니다. 자만하지 말고 낮은 자세로 임하세요. 다른 사람을 도우면 그 덕이 자신에게도 돌아올 것입니다.", 84));

        TOJEONG_GWA_MAP.put(15, new TojeongGwa("뢰지예", "☳☷", "즐거움과 기쁨이 찾아온다",
                "기쁘고 즐거운 일들이 많은 해입니다. 문화활동이나 취미생활에서 즐거움을 찾을 수 있고, 경사스러운 일도 있을 것입니다. 다만 지나친 쾌락에는 주의하세요.", 81));

        // ===== 중상괫 (中上卦) - 중길 (55-69점) =====
        TOJEONG_GWA_MAP.put(16, new TojeongGwa("택뢰수", "☱☳", "따라가며 순응하면 길하다",
                "상황에 순응하며 흐름을 따라가는 것이 좋은 해입니다. 억지로 밀어붙이지 말고 자연스럽게 흘러가도록 하세요. 윗사람의 뜻을 잘 받들면 좋은 결과가 있을 것입니다.", 68));

        TOJEONG_GWA_MAP.put(17, new TojeongGwa("산택손", "☶☱", "손해를 보더라도 나중에 큰 이득이 온다",
                "당장은 손해를 보는 것 같지만 장기적으로는 큰 이익이 있을 것입니다. 단기적 손실에 연연하지 말고 장기적 관점에서 판단하세요. 남을 위한 희생이 결국 자신의 이익으로 돌아올 것입니다.", 65));

        TOJEONG_GWA_MAP.put(18, new TojeongGwa("풍산점", "☴☶", "점진적으로 나아가면 길하다",
                "천천히 단계적으로 발전시켜 나가는 것이 좋습니다. 급하게 서두르지 말고 차근차근 기반을 다지세요. 꾸준한 노력이 결국 큰 성과로 이어질 것입니다.", 70));

        TOJEONG_GWA_MAP.put(19, new TojeongGwa("택풍대과", "☱☴", "큰 시련이 있으나 극복할 수 있다",
                "평소보다 어려운 상황에 직면할 수 있지만 의지력으로 극복 가능합니다. 위기를 기회로 바꿀 수 있는 지혜가 필요한 시기입니다. 주변의 조언을 잘 들으세요.", 62));

        TOJEONG_GWA_MAP.put(20, new TojeongGwa("감위수", "☵☵", "위험이 겹치지만 지혜로 헤쳐나간다",
                "어려운 상황이 연속으로 오지만 지혜롭게 대처하면 해결됩니다. 신중하게 판단하고 무리한 모험은 피하세요. 물과 관련된 일은 특히 조심해야 합니다.", 58));

        TOJEONG_GWA_MAP.put(21, new TojeongGwa("리위화", "☲☲", "밝은 지혜로 모든 것을 비춘다",
                "지혜와 학문이 빛을 발하는 해입니다. 공부하거나 연구하는 일에 좋은 성과가 있을 것이며, 창조적인 아이디어도 돋보일 것입니다. 교육이나 출판 관련 일에 좋습니다.", 74));

        TOJEONG_GWA_MAP.put(22, new TojeongGwa("산화비", "☶☲", "겉모습을 아름답게 꾸미는 때",
                "외관을 정비하고 이미지를 개선하는 데 좋은 해입니다. 패션이나 미용, 인테리어 등에 관심을 가져보세요. 다만 겉모습만 치중하지 말고 내면도 함께 가꾸어야 합니다.", 67));

        TOJEONG_GWA_MAP.put(23, new TojeongGwa("산지박", "☶☷", "벗겨내고 새롭게 시작한다",
                "기존의 것을 과감히 정리하고 새롭게 시작하는 해입니다. 불필요한 것들을 정리하고 핵심만 남겨두세요. 단순하고 소박한 삶이 오히려 행복을 가져다줄 것입니다.", 63));

        // ===== 중괘 (中卦) - 보통 (40-54점) =====
        TOJEONG_GWA_MAP.put(24, new TojeongGwa("지뢰복", "☷☳", "다시 돌아와 새로운 시작을 한다",
                "잃었던 것이 다시 돌아오거나 새로운 기회가 찾아오는 해입니다. 과거의 실패를 교훈으로 삼아 다시 도전해보세요. 겨울이 지나고 봄이 오듯 새로운 희망이 생길 것입니다.", 72));

        TOJEONG_GWA_MAP.put(25, new TojeongGwa("천뢰무망", "☰☳", "망령된 생각을 버리고 순수하게 행동한다",
                "순수하고 진실한 마음으로 행동하면 좋은 결과를 얻을 것입니다. 거짓이나 속임수는 피하고 정직하게 살아가세요. 자연스럽고 소박한 삶의 지혜가 필요한 때입니다.", 69));

        TOJEONG_GWA_MAP.put(26, new TojeongGwa("산천대축", "☶☰", "큰 힘을 기르며 축적한다",
                "실력과 역량을 기르는 데 집중하는 해입니다. 당장의 성과보다는 장기적인 실력 향상에 투자하세요. 교육이나 훈련을 받으면 나중에 큰 도움이 될 것입니다.", 71));

        TOJEONG_GWA_MAP.put(27, new TojeongGwa("산뢰이", "☶☳", "올바른 양육과 교육의 때",
                "자신을 기르고 남을 가르치는 일에 좋은 해입니다. 말과 행동을 조심하고 올바른 생활습관을 기르세요. 건강관리와 식생활에 특히 신경 써야 합니다.", 66));

        TOJEONG_GWA_MAP.put(28, new TojeongGwa("택풍대과", "☱☴", "큰 어려움이 있지만 극복 가능하다",
                "평상시보다 큰 부담이나 어려움이 있을 수 있지만 슬기롭게 헤쳐나갈 수 있습니다. 무리하지 말고 자신의 능력 범위 내에서 차근차근 해결해 나가세요.", 58));

        TOJEONG_GWA_MAP.put(29, new TojeongGwa("감위수", "☵☵", "위험이 거듭되니 매우 조심해야 한다",
                "어려운 상황이 계속될 수 있으니 신중하게 행동해야 합니다. 모험적인 일은 피하고 안전한 길을 택하세요. 물이나 액체와 관련된 일에는 특별히 주의가 필요합니다.", 45));

        TOJEONG_GWA_MAP.put(30, new TojeongGwa("리위화", "☲☲", "밝음이 계속되어 크게 형통하다",
                "지혜와 재능이 빛을 발하는 해입니다. 창작활동이나 학술연구에 좋은 성과가 있을 것이며, 유명세를 탈 수도 있습니다. 밝고 긍정적인 마음가짐을 유지하세요.", 79));

        TOJEONG_GWA_MAP.put(31, new TojeongGwa("택산함", "☱☶", "서로 감응하여 좋은 관계를 맺는다",
                "인간관계에서 좋은 감정의 교류가 있는 해입니다. 이성관계나 결혼에 특히 좋은 시기이며, 사업 파트너십도 잘 이루어질 것입니다. 진실한 마음으로 대하세요.", 77));

        // ===== 중하괘 (中下卦) - 평 (25-39점) =====
        TOJEONG_GWA_MAP.put(32, new TojeongGwa("뢰풍항", "☳☴", "꾸준히 지속하면 성공한다",
                "꾸준함과 지속성이 중요한 해입니다. 작은 성과에 만족하지 말고 끝까지 밀고 나가세요. 중도에 포기하지 않는 의지력이 성공의 열쇠가 될 것입니다.", 64));

        TOJEONG_GWA_MAP.put(33, new TojeongGwa("천산둔", "☰☶", "물러나서 때를 기다린다",
                "적극적으로 나서지 말고 물러나서 때를 기다리는 것이 좋습니다. 무리한 추진보다는 현상유지에 만족하고 실력을 기르는 시간으로 활용하세요.", 52));

        TOJEONG_GWA_MAP.put(34, new TojeongGwa("뢰천대장", "☳☰", "큰 힘을 얻어 크게 발전한다",
                "강한 의지력과 추진력을 발휘할 수 있는 해입니다. 적극적으로 도전하고 자신의 능력을 마음껏 발휘하세요. 리더십을 발휘할 기회도 많을 것입니다.", 84));

        TOJEONG_GWA_MAP.put(35, new TojeongGwa("화지진", "☲☷", "밝은 빛이 땅 위에 비친다",
                "자신의 재능이 세상에 알려지는 해입니다. 승진이나 인정을 받을 기회가 많고, 사회적 지위도 향상될 것입니다. 겸손함을 잃지 않도록 주의하세요.", 82));

        TOJEONG_GWA_MAP.put(36, new TojeongGwa("지화명이", "☷☲", "밝음이 상처받는 어려운 시기",
                "어려운 상황에 처할 수 있지만 내면의 밝음을 잃지 마세요. 겉으로는 힘들어 보여도 마음의 평정을 유지하면 결국 극복할 수 있습니다. 건강관리에 특히 신경 쓰세요.", 48));

        TOJEONG_GWA_MAP.put(37, new TojeongGwa("풍화가인", "☴☲", "집안이 화목하고 가족이 단합한다",
                "가정이나 조직 내부의 화합이 이루어지는 해입니다. 가족관계가 좋아지고 직장 동료들과의 관계도 원만해질 것입니다. 집안일이나 내부 정리에 신경 쓰세요.", 73));

        TOJEONG_GWA_MAP.put(38, new TojeongGwa("화택규", "☲☱", "서로 등지고 있지만 작은 일은 형통하다",
                "큰 일보다는 작은 일에 집중하는 것이 좋은 해입니다. 의견 차이나 갈등이 있을 수 있지만 작은 부분에서는 협력이 가능합니다. 너무 큰 욕심을 부리지 마세요.", 56));

        TOJEONG_GWA_MAP.put(39, new TojeongGwa("수산건", "☵☶", "어려움에 막혀 있지만 도움을 받는다",
                "어려운 상황이지만 주변의 도움을 받아 해결할 수 있습니다. 혼자 해결하려 하지 말고 도움을 요청하세요. 겸손한 자세로 조언을 구하면 좋은 해답을 얻을 것입니다.", 61));

        // ===== 하괘 (下卦) - 흉 (10-24점) =====
        TOJEONG_GWA_MAP.put(40, new TojeongGwa("뢰수해", "☳☵", "얽힌 것이 풀리고 해방된다",
                "복잡하게 얽혔던 문제들이 서서히 해결되는 해입니다. 답답했던 상황에서 벗어날 수 있고, 새로운 전기를 마련할 수 있습니다. 과거에 얽매이지 말고 앞으로 나아가세요.", 67));

        TOJEONG_GWA_MAP.put(41, new TojeongGwa("산택손", "☶☱", "줄이고 덜어내어 검소함을 지킨다",
                "욕심을 줄이고 불필요한 것들을 정리하는 해입니다. 검소하고 절약하는 생활이 나중에 큰 도움이 될 것입니다. 물질적 풍요보다는 정신적 만족을 추구하세요.", 59));

        TOJEONG_GWA_MAP.put(42, new TojeongGwa("풍뢰익", "☴☳", "더하고 늘려서 이익을 얻는다",
                "노력한 만큼 성과를 거둘 수 있는 해입니다. 투자나 확장에 좋은 시기이며, 새로운 분야에 도전해볼 만합니다. 다른 사람을 도우면 그 덕이 자신에게도 돌아올 것입니다.", 76));

        TOJEONG_GWA_MAP.put(43, new TojeongGwa("택천쾌", "☱☰", "결단력 있게 행동하면 형통하다",
                "과감한 결단력이 필요한 해입니다. 망설이지 말고 옳다고 생각하는 일은 추진하세요. 다만 독단적이 되지 말고 주변의 의견도 들어보는 것이 좋습니다.", 71));

        TOJEONG_GWA_MAP.put(44, new TojeongGwa("천풍구", "☰☴", "뜻밖의 만남이나 기회가 온다",
                "예상치 못한 만남이나 기회가 찾아올 수 있습니다. 새로운 인연에 열린 마음으로 임하되, 너무 성급하게 판단하지는 마세요. 신중하면서도 유연한 자세가 필요합니다.", 63));

        TOJEONG_GWA_MAP.put(45, new TojeongGwa("택지취", "☱☷", "모여서 힘을 합친다",
                "여러 사람이 모여 힘을 합치는 일에 좋은 해입니다. 단체 활동이나 조직적인 일에서 성과를 거둘 것입니다. 리더십을 발휘하거나 팀워크를 중시하는 자세가 중요합니다.", 78));

        TOJEONG_GWA_MAP.put(46, new TojeongGwa("지풍승", "☷☴", "점점 상승하여 높은 곳에 이른다",
                "서서히 상승하는 운세로 꾸준한 발전이 있을 것입니다. 급하게 서두르지 말고 단계적으로 올라가세요. 목표를 높게 설정하고 차근차근 노력하면 반드시 성취할 수 있습니다.", 80));

        TOJEONG_GWA_MAP.put(47, new TojeongGwa("택수곤", "☱☵", "어려움에 곤궁하지만 의지를 잃지 않는다",
                "일시적으로 어려운 상황에 처할 수 있지만 포기하지 마세요. 현재의 고난은 미래의 성공을 위한 시련입니다. 의지력을 잃지 않고 끝까지 버티면 반드시 기회가 올 것입니다.", 54));

        TOJEONG_GWA_MAP.put(48, new TojeongGwa("수풍정", "☵☴", "우물을 파서 맑은 물을 얻는다",
                "깊이 있는 학문이나 기술을 연마하는 데 좋은 해입니다. 표면적인 것보다는 본질을 파고드는 노력이 필요합니다. 전문성을 기르면 나중에 큰 도움이 될 것입니다.", 68));

        TOJEONG_GWA_MAP.put(49, new TojeongGwa("택화혁", "☱☲", "변혁과 개혁의 시기가 온다",
                "큰 변화와 개혁이 필요한 해입니다. 기존의 방식을 과감히 바꾸고 새로운 시도를 해보세요. 변화에 대한 두려움을 버리고 적극적으로 개혁에 나서면 좋은 결과가 있을 것입니다.", 75));

        TOJEONG_GWA_MAP.put(50, new TojeongGwa("화풍정", "☲☴", "솥처럼 안정되고 조화롭다",
                "안정되고 조화로운 상태를 유지할 수 있는 해입니다. 가정이나 사업이 안정되고 수입도 꾸준할 것입니다. 전통적인 방법을 고수하면서 점진적인 발전을 추구하세요.", 81));

        TOJEONG_GWA_MAP.put(51, new TojeongGwa("진위뢰", "☳☳", "우뢰가 크게 울려 놀라게 한다",
                "갑작스런 변화나 충격적인 사건이 있을 수 있습니다. 처음에는 당황스럽겠지만 이를 계기로 새로운 전기를 마련할 수 있습니다. 변화에 잘 적응하는 것이 중요합니다.", 62));

        TOJEONG_GWA_MAP.put(52, new TojeongGwa("간위산", "☶☶", "산처럼 그치고 멈춰서 때를 기다린다",
                "무리하게 앞으로 나아가지 말고 현 상황에서 머물며 실력을 기르는 시기입니다. 급한 마음을 버리고 차분하게 준비하세요. 때가 되면 자연스럽게 기회가 올 것입니다.", 57));

        TOJEONG_GWA_MAP.put(53, new TojeongGwa("풍산점", "☴☶", "점진적으로 천천히 나아간다",
                "서두르지 말고 단계적으로 발전시켜 나가는 것이 좋습니다. 큰 성취보다는 작은 진보를 축적하는 데 집중하세요. 꾸준한 노력이 결국 큰 성과로 이어질 것입니다.", 69));

        TOJEONG_GWA_MAP.put(54, new TojeongGwa("뢰택귀매", "☳☱", "귀한 여인이 시집가는 길한 때",
                "새로운 인연이나 결합이 이루어지는 해입니다. 결혼이나 사업 파트너십에 좋은 시기이며, 상호 보완적인 관계를 맺을 수 있습니다. 서로를 존중하는 마음이 중요합니다.", 74));

        TOJEONG_GWA_MAP.put(55, new TojeongGwa("뢰화풍", "☳☲", "번영과 풍요가 절정에 이른다",
                "모든 면에서 풍요롭고 번영하는 최고의 해입니다. 사업이나 재물운이 크게 상승하고 명예도 얻을 수 있습니다. 다만 절정의 때이므로 겸손함을 잃지 않도록 주의해야 합니다.", 92));

        TOJEONG_GWA_MAP.put(56, new TojeongGwa("화산려", "☲☶", "나그네처럼 떠돌지만 예의를 지킨다",
                "이동이나 변화가 많은 해입니다. 출장이나 이사, 전직 등이 있을 수 있지만 나쁘지 않은 결과를 가져다줄 것입니다. 어디를 가든 예의와 겸손함을 잃지 마세요.", 65));

        TOJEONG_GWA_MAP.put(57, new TojeongGwa("손위풍", "☴☴", "부드러운 바람처럼 은근히 스며든다",
                "부드럽고 온화한 방법으로 목표를 달성하는 해입니다. 강압적이지 말고 설득과 협조로 일을 처리하세요. 여성의 도움이나 부드러운 리더십이 효과적일 것입니다.", 71));

        TOJEONG_GWA_MAP.put(58, new TojeongGwa("태위택", "☱☱", "기쁨과 즐거움이 가득한 때",
                "기쁘고 즐거운 일들이 많이 생기는 해입니다. 사교활동이나 문화생활을 통해 즐거움을 찾을 수 있고, 좋은 인연도 만날 수 있습니다. 다만 과도한 쾌락은 피하세요.", 83));

        TOJEONG_GWA_MAP.put(59, new TojeongGwa("풍수환", "☴☵", "흩어진 것들이 다시 모인다",
                "흩어지고 분산되었던 것들이 다시 모이는 해입니다. 잃었던 것을 되찾거나 헤어졌던 사람과 재회할 수 있습니다. 단합과 협력이 중요한 시기입니다.", 66));

        TOJEONG_GWA_MAP.put(60, new TojeongGwa("수택절", "☵☱", "절제하고 제한하여 질서를 세운다",
                "절제와 자제력이 필요한 해입니다. 욕심을 부리지 말고 적당한 선에서 만족하세요. 규칙과 원칙을 지키면서 질서 있게 생활하는 것이 중요합니다.", 58));

        TOJEONG_GWA_MAP.put(61, new TojeongGwa("풍택중부", "☴☱", "진실한 마음으로 신의를 지킨다",
                "진실하고 성실한 마음가짐이 중요한 해입니다. 약속을 지키고 신뢰를 쌓으면 좋은 결과가 있을 것입니다. 작은 일에도 정성을 다하는 자세가 필요합니다.", 77));

        TOJEONG_GWA_MAP.put(62, new TojeongGwa("뢰산소과", "☳☶", "작은 것에서 지나침이 있다",
                "작은 일에는 적극적이어도 좋지만 큰 일에는 신중해야 하는 해입니다. 세부사항에 신경 쓰되 전체적인 균형을 잃지 마세요. 완벽주의가 오히려 독이 될 수 있습니다.", 60));

        TOJEONG_GWA_MAP.put(63, new TojeongGwa("수화기제", "☵☲", "이미 완성되었지만 계속 노력해야 한다",
                "목표를 달성했지만 방심하지 말고 계속 노력해야 하는 해입니다. 현재의 성취에 안주하지 말고 더 높은 목표를 설정하세요. 완성 후의 관리가 더욱 중요합니다.", 73));

        TOJEONG_GWA_MAP.put(64, new TojeongGwa("화수미제", "☲☵", "아직 완성되지 않았으니 계속 정진한다",
                "아직 완성되지 않은 상태이므로 계속 노력해야 하는 해입니다. 포기하지 말고 끝까지 최선을 다하세요. 목표 달성까지 얼마 남지 않았으니 조금만 더 인내하면 성공할 것입니다.", 68));

        // ===== 하하괘 (下下卦) - 대흉 (0-9점) =====
        // 64괘 중에서는 특별히 대흉한 괘는 없으며, 모든 괘에는 나름의 의미와 교훈이 있습니다.
        // 가장 어려운 괘들도 최소 40점 이상의 의미를 가지고 있습니다.
    }

    /**
     * 토정비결 계산 메인 메서드
     *
     * @param birthYear 생년
     * @param birthMonth 생월
     * @param birthDay 생일
     * @param targetYear 대상 년도
     * @return 토정비결 결과
     */
    public TojeongResult calculateTojeong(int birthYear, int birthMonth, int birthDay, int targetYear) {
        // 1. 토정비결 고유 알고리즘
        int sum = birthYear + birthMonth + birthDay + targetYear;

        // 2. 복잡한 계산 과정 (토정비결 전통 공식)
        int step1 = (sum % 60) + 1;
        int step2 = (step1 * birthDay) % 100;
        int step3 = (step2 + birthMonth) % 64;

        // 3. 최종 괘 선택
        int gwaIndex = step3;
        TojeongGwa selectedGwa = TOJEONG_GWA_MAP.getOrDefault(gwaIndex, TOJEONG_GWA_MAP.get(0));

        // 4. 월별 세부 운세 생성
        Map<Integer, String> monthlyFortune = generateMonthlyFortune(gwaIndex, targetYear);

        // 5. 계절별 운세 생성
        Map<String, String> seasonalFortune = generateSeasonalFortune(gwaIndex);

        return TojeongResult.builder()
                .targetYear(targetYear)
                .gwaNumber(gwaIndex)  // 괘 번호 추가
                .gwaName(selectedGwa.getName())
                .gwaSymbol(selectedGwa.getSymbol())
                .summary(selectedGwa.getSummary())
                .detailedFortune(selectedGwa.getDetailedFortune())
                .monthlyFortune(monthlyFortune)
                .seasonalFortune(seasonalFortune)
                .overallScore(selectedGwa.getOverallScore())
                .advice(generateYearlyAdvice(gwaIndex, selectedGwa.getOverallScore()))
                .luckyMonths(getLuckyMonths(gwaIndex))
                .cautionMonths(getCautionMonths(gwaIndex))
                .build();
    }

    /**
     * 월별 상세 운세 생성
     * @param gwaIndex 괘 인덱스
     * @param year 년도
     * @return 월별 상세 운세
     */
    private Map<Integer, String> generateMonthlyFortune(int gwaIndex, int year) {
        Map<Integer, String> monthlyFortune = new HashMap<>();

        String[] monthNames = {"", "정월", "이월", "삼월", "사월", "오월", "유월",
                "칠월", "팔월", "구월", "시월", "동월", "섣달"};

        for (int month = 1; month <= 12; month++) {
            int monthlyGwaIndex = (gwaIndex + month * 3) % 64;
            TojeongGwa monthlyGwa = TOJEONG_GWA_MAP.get(monthlyGwaIndex);

            String monthlyDescription;
            if (monthlyGwa.getOverallScore() >= 80) {
                monthlyDescription = monthNames[month] + "에는 " + monthlyGwa.getName() + "의 기운으로 매우 길합니다. " +
                        "새로운 일을 시작하거나 중요한 결정을 내리기 좋은 시기입니다.";
            } else if (monthlyGwa.getOverallScore() >= 60) {
                monthlyDescription = monthNames[month] + "에는 " + monthlyGwa.getName() + "의 기운으로 평안합니다. " +
                        "꾸준히 노력하면 좋은 결과를 얻을 수 있습니다.";
            } else if (monthlyGwa.getOverallScore() >= 40) {
                monthlyDescription = monthNames[month] + "에는 " + monthlyGwa.getName() + "의 기운으로 보통입니다. " +
                        "신중하게 행동하고 무리하지 마세요.";
            } else {
                monthlyDescription = monthNames[month] + "에는 " + monthlyGwa.getName() + "의 기운으로 조심스럽습니다. " +
                        "중요한 일은 미루고 안전에 유의하세요.";
            }

            monthlyFortune.put(month, monthlyDescription);
        }

        return monthlyFortune;
    }

    /**
     * 계절별 운세 생성
     * @param gwaIndex 괘 인덱스
     * @return 계절별 운세
     */
    private Map<String, String> generateSeasonalFortune(int gwaIndex) {
        Map<String, String> seasonalFortune = new HashMap<>();
        TojeongGwa mainGwa = TOJEONG_GWA_MAP.get(gwaIndex);

        // 봄 (3-5월)
        int springScore = Math.max(10, Math.min(100, (mainGwa.getOverallScore() + (gwaIndex % 20))));
        seasonalFortune.put("봄", generateSeasonDescription("봄", springScore, "새로운 시작과 성장"));

        // 여름 (6-8월)
        int summerScore = Math.max(10, Math.min(100, (mainGwa.getOverallScore() + (gwaIndex % 25))));
        seasonalFortune.put("여름", generateSeasonDescription("여름", summerScore, "활발한 활동과 번영"));

        // 가을 (9-11월)
        int autumnScore = Math.max(10, Math.min(100, (mainGwa.getOverallScore() + (gwaIndex % 30))));
        seasonalFortune.put("가을", generateSeasonDescription("가을", autumnScore, "수확과 결실"));

        // 겨울 (12-2월)
        int winterScore = Math.max(10, Math.min(100, (mainGwa.getOverallScore() + (gwaIndex % 15))));
        seasonalFortune.put("겨울", generateSeasonDescription("겨울", winterScore, "휴식과 준비"));

        return seasonalFortune;
    }

    /**
     * 계절별 운세 생성
     * @param season 계절
     * @param score 점수
     * @param theme 주제
     * @return 계절별 운세
     */
    private String generateSeasonDescription(String season, int score, String theme) {
        if (score >= 80) {
            return season + "철에는 " + theme + "의 기운이 매우 강합니다. 적극적으로 활동하세요.";
        } else if (score >= 60) {
            return season + "철에는 " + theme + "의 기운이 좋습니다. 계획적으로 행동하세요.";
        } else if (score >= 40) {
            return season + "철에는 " + theme + "의 기운이 보통입니다. 신중하게 판단하세요.";
        } else {
            return season + "철에는 " + theme + "의 기운이 약합니다. 조심스럽게 행동하세요.";
        }
    }

    /**
     * 연간 종합 조언 생성
     * @param gwaIndex 괘 인덱스
     * @param overallScore 종합 점수
     * @return 연간 종합 조언
     */
    private String generateYearlyAdvice(int gwaIndex, int overallScore) {
        TojeongGwa mainGwa = TOJEONG_GWA_MAP.get(gwaIndex);

        StringBuilder advice = new StringBuilder();
        advice.append("올해 당신의 운세는 '").append(mainGwa.getName()).append("'괘입니다. ");

        if (overallScore >= 90) {
            advice.append("최상의 운세로 모든 일에 적극적으로 도전하세요. ");
            advice.append("큰 계획을 세우고 실행에 옮기기 좋은 해입니다.");
        } else if (overallScore >= 80) {
            advice.append("매우 좋은 운세입니다. 새로운 기회를 놓치지 마세요. ");
            advice.append("자신감을 갖고 추진력 있게 행동하세요.");
        } else if (overallScore >= 70) {
            advice.append("좋은 운세입니다. 꾸준한 노력이 결실을 맺을 것입니다. ");
            advice.append("인내심을 갖고 차근차근 발전시켜 나가세요.");
        } else if (overallScore >= 60) {
            advice.append("평균적인 운세입니다. 안정적으로 현상을 유지하세요. ");
            advice.append("무리한 도전보다는 실력을 기르는 데 집중하세요.");
        } else if (overallScore >= 50) {
            advice.append("조심스러운 운세입니다. 신중하게 판단하고 행동하세요. ");
            advice.append("큰 변화보다는 현재 상황을 지키는 데 집중하세요.");
        } else {
            advice.append("어려운 운세이지만 포기하지 마세요. ");
            advice.append("겸손한 마음으로 기본에 충실하면 운세가 점차 호전될 것입니다.");
        }

        return advice.toString();
    }

    /**
     * 길한 달 선별
     * @param gwaIndex 괘 인덱스
     * @return 길한 달
     */
    private List<Integer> getLuckyMonths(int gwaIndex) {
        List<Integer> luckyMonths = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            int monthlyGwaIndex = (gwaIndex + month * 3) % 64;
            TojeongGwa monthlyGwa = TOJEONG_GWA_MAP.get(monthlyGwaIndex);

            if (monthlyGwa.getOverallScore() >= 75) {
                luckyMonths.add(month);
            }
        }

        return luckyMonths;
    }

    /**
     * 주의할 달 선별
     * @param gwaIndex 괘 인덱스
     * @return 주의할 달
     */
    private List<Integer> getCautionMonths(int gwaIndex) {
        List<Integer> cautionMonths = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            int monthlyGwaIndex = (gwaIndex + month * 3) % 64;
            TojeongGwa monthlyGwa = TOJEONG_GWA_MAP.get(monthlyGwaIndex);

            if (monthlyGwa.getOverallScore() < 50) {
                cautionMonths.add(month);
            }
        }

        return cautionMonths;
    }
    
//    static {
//        // ===== 상상괘 (上上卦) - 대길 (85-100점) =====
//        TOJEONG_GWA_MAP.put(0, new TojeongGwa("건위천", "☰☰", "하늘이 도우니 크게 길하다",
//            "올해는 하늘의 도움을 받아 모든 일이 순조롭게 풀릴 것입니다. 적극적으로 도전하고 새로운 사업이나 계획을 시작하기에 최적의 해입니다. 특히 리더십을 발휘할 기회가 많고, 주변의 도움도 받을 수 있습니다. 건강도 좋고 재물운도 상승합니다.", 95));
//
//        TOJEONG_GWA_MAP.put(1, new TojeongGwa("곤위지", "☷☷", "땅의 덕으로 차근차근 발전한다",
//            "꾸준함과 인내심이 빛을 발하는 해입니다. 급하게 서두르지 말고 기초를 탄탄히 다지세요. 여성이나 어머니의 도움이 클 것입니다. 부동산이나 안정적인 투자에 좋은 시기이며, 가정의 화목도 증진됩니다.", 88));
//
//        TOJEONG_GWA_MAP.put(2, new TojeongGwa("수뢰둔", "☵☳", "어려움 속에서도 희망이 보인다",
//            "초반에는 어려움이 있지만 꾸준히 노력하면 좋은 결과가 있을 것입니다. 새로운 시작의 해로 인내심을 갖고 기다리세요. 학업이나 새로운 기술 습득에 좋은 시기입니다.", 75));
//
//        TOJEONG_GWA_MAP.put(3, new TojeongGwa("산수몽", "☶☵", "배움을 통해 지혜를 얻는다",
//            "학습과 수양에 좋은 해입니다. 선배나 스승의 가르침을 받아들이면 큰 발전이 있을 것입니다. 젊은 사람들에게는 특히 학업운이 상승하며, 새로운 지식이나 기술을 익히기 좋습니다.", 82));
//
//        TOJEONG_GWA_MAP.put(4, new TojeongGwa("수천수", "☵☰", "기다림 후에 큰 성취가 온다",
//            "참고 기다리면 반드시 좋은 결과가 있을 것입니다. 성급하게 서두르지 말고 때를 기다리세요. 상반기는 준비 기간이고 하반기부터 본격적인 성과가 나타날 것입니다.", 78));
//
//        TOJEONG_GWA_MAP.put(5, new TojeongGwa("천수송", "☰☵", "분쟁이 있으나 결국 해결된다",
//            "갈등이나 문제가 있을 수 있지만 지혜롭게 대처하면 해결됩니다. 법적 문제나 계약 관련해서는 신중하게 접근하세요. 중재자의 도움을 받으면 좋은 결과를 얻을 수 있습니다.", 70));
//
//        TOJEONG_GWA_MAP.put(6, new TojeongGwa("지수사", "☷☵", "많은 사람이 도와준다",
//            "주변 사람들의 도움과 협력을 얻을 수 있는 해입니다. 팀워크나 조직력이 중요한 시기로, 혼자보다는 여러 사람과 함께하는 일에서 성과를 거둘 것입니다. 인맥 관리에 신경 쓰세요.", 85));
//
//        TOJEONG_GWA_MAP.put(7, new TojeongGwa("수지비", "☵☷", "서로 가까워지는 좋은 시기",
//            "인간관계가 원활해지고 협력이 잘 이루어지는 해입니다. 결혼이나 파트너십에 좋은 시기이며, 사업상 좋은 파트너를 만날 수 있습니다. 외교적 능력이 빛을 발할 것입니다.", 87));
//
//        // ===== 상괘 (上卦) - 길 (70-84점) =====
//        TOJEONG_GWA_MAP.put(8, new TojeongGwa("풍천소축", "☴☰", "작은 것을 모아 큰 것을 이룬다",
//            "작은 노력들이 쌓여서 큰 성과를 만드는 해입니다. 절약과 저축이 중요하며, 꾸준한 노력이 결실을 맺을 것입니다. 세부적인 것에 신경 쓰고 계획적으로 행동하세요.", 76));
//
//        TOJEONG_GWA_MAP.put(9, new TojeongGwa("천택리", "☰☱", "예의와 질서를 지키면 형통하다",
//            "질서와 예의를 지키면서 행동하면 좋은 결과를 얻을 것입니다. 사회적 지위가 향상되고 품격 있는 행동이 인정받을 것입니다. 전통이나 관습을 존중하는 자세가 중요합니다.", 80));
//
//        TOJEONG_GWA_MAP.put(10, new TojeongGwa("지천태", "☷☰", "하늘과 땅이 화합하여 크게 형통하다",
//            "모든 일이 순조롭게 풀리는 최고의 해입니다. 상하가 화합하고 모든 관계가 원만해집니다. 새로운 사업을 시작하거나 중요한 결정을 내리기에 최적의 시기입니다.", 95));
//
//        TOJEONG_GWA_MAP.put(11, new TojeongGwa("천지비", "☰☷", "막힌 것이 뚫리기 시작한다",
//            "막혔던 일들이 서서히 풀리기 시작하는 해입니다. 인내심을 갖고 기다리면 좋은 결과가 있을 것입니다. 아직은 조심스럽게 행동하되, 희망을 잃지 마세요.", 72));
//
//        TOJEONG_GWA_MAP.put(12, new TojeongGwa("천화동인", "☰☲", "뜻을 같이하는 사람들과 함께 한다",
//            "같은 목표를 가진 사람들과의 협력이 중요한 해입니다. 동료나 친구들과의 관계가 좋아지고, 함께하는 일에서 큰 성과를 거둘 것입니다. 네트워킹에 신경 쓰세요.", 83));
//
//        TOJEONG_GWA_MAP.put(13, new TojeongGwa("화천대유", "☲☰", "큰 소유를 얻게 된다",
//            "재물운이 크게 상승하는 해입니다. 투자나 사업에서 좋은 성과를 거둘 것이며, 큰 수입이나 재산을 얻을 가능성이 높습니다. 다만 겸손함을 잃지 않도록 주의하세요.", 90));
//
//        TOJEONG_GWA_MAP.put(14, new TojeongGwa("지산겸", "☷☶", "겸손하면 모든 것이 형통하다",
//            "겸손한 마음가짐으로 임하면 모든 일이 잘 풀리는 해입니다. 자만하지 말고 낮은 자세로 임하세요. 다른 사람을 도우면 그 덕이 자신에게도 돌아올 것입니다.", 84));
//
//        TOJEONG_GWA_MAP.put(15, new TojeongGwa("뢰지예", "☳☷", "즐거움과 기쁨이 찾아온다",
//            "기쁘고 즐거운 일들이 많은 해입니다. 문화활동이나 취미생활에서 즐거움을 찾을 수 있고, 경사스러운 일도 있을 것입니다. 다만 지나친 쾌락에는 주의하세요.", 81));
//
//        // ===== 중상괫 (中上卦) - 중길 (55-69점) =====
//        TOJEONG_GWA_MAP.put(16, new TojeongGwa("택뢰수", "☱☳", "따라가며 순응하면 길하다",
//            "상황에 순응하며 흐름을 따라가는 것이 좋은 해입니다. 억지로 밀어붙이지 말고 자연스럽게 흘러가도록 하세요. 윗사람의 뜻을 잘 받들면 좋은 결과가 있을 것입니다.", 68));
//
//        TOJEONG_GWA_MAP.put(17, new TojeongGwa("산택손", "☶☱", "손해를 보더라도 나중에 큰 이득이 온다",
//            "당장은 손해를 보는 것 같지만 장기적으로는 큰 이익이 있을 것입니다. 단기적 손실에 연연하지 말고 장기적 관점에서 판단하세요. 남을 위한 희생이 결국 자신의 이익으로 돌아올 것입니다.", 65));
//
//        TOJEONG_GWA_MAP.put(18, new TojeongGwa("풍산점", "☴☶", "점진적으로 나아가면 길하다",
//            "천천히 단계적으로 발전시켜 나가는 것이 좋습니다. 급하게 서두르지 말고 차근차근 기반을 다지세요. 꾸준한 노력이 결국 큰 성과로 이어질 것입니다.", 70));
//
//        TOJEONG_GWA_MAP.put(19, new TojeongGwa("택풍대과", "☱☴", "큰 시련이 있으나 극복할 수 있다",
//            "평소보다 어려운 상황에 직면할 수 있지만 의지력으로 극복 가능합니다. 위기를 기회로 바꿀 수 있는 지혜가 필요한 시기입니다. 주변의 조언을 잘 들으세요.", 62));
//
//        TOJEONG_GWA_MAP.put(20, new TojeongGwa("감위수", "☵☵", "위험이 겹치지만 지혜로 헤쳐나간다",
//            "어려운 상황이 연속으로 오지만 지혜롭게 대처하면 해결됩니다. 신중하게 판단하고 무리한 모험은 피하세요. 물과 관련된 일은 특히 조심해야 합니다.", 58));
//
//        TOJEONG_GWA_MAP.put(21, new TojeongGwa("리위화", "☲☲", "밝은 지혜로 모든 것을 비춘다",
//            "지혜와 학문이 빛을 발하는 해입니다. 공부하거나 연구하는 일에 좋은 성과가 있을 것이며, 창조적인 아이디어도 돋보일 것입니다. 교육이나 출판 관련 일에 좋습니다.", 74));
//
//        TOJEONG_GWA_MAP.put(22, new TojeongGwa("산화비", "☶☲", "겉모습을 아름답게 꾸미는 때",
//            "외관을 정비하고 이미지를 개선하는 데 좋은 해입니다. 패션이나 미용, 인테리어 등에 관심을 가져보세요. 다만 겉모습만 치중하지 말고 내면도 함께 가꾸어야 합니다.", 67));
//
//        TOJEONG_GWA_MAP.put(23, new TojeongGwa("산지박", "☶☷", "벗겨내고 새롭게 시작한다",
//            "기존의 것을 과감히 정리하고 새롭게 시작하는 해입니다. 불필요한 것들을 정리하고 핵심만 남겨두세요. 단순하고 소박한 삶이 오히려 행복을 가져다줄 것입니다.", 63));
//
//        // ===== 중괘 (中卦) - 보통 (40-54점) =====
//        TOJEONG_GWA_MAP.put(24, new TojeongGwa("지뢰복", "☷☳", "다시 돌아와 새로운 시작을 한다",
//            "잃었던 것이 다시 돌아오거나 새로운 기회가 찾아오는 해입니다. 과거의 실패를 교훈으로 삼아 다시 도전해보세요. 겨울이 지나고 봄이 오듯 새로운 희망이 생길 것입니다.", 72));
//
//        TOJEONG_GWA_MAP.put(25, new TojeongGwa("천뢰무망", "☰☳", "망령된 생각을 버리고 순수하게 행동한다",
//            "순수하고 진실한 마음으로 행동하면 좋은 결과를 얻을 것입니다. 거짓이나 속임수는 피하고 정직하게 살아가세요. 자연스럽고 소박한 삶의 지혜가 필요한 때입니다.", 69));
//
//        TOJEONG_GWA_MAP.put(26, new TojeongGwa("산천대축", "☶☰", "큰 힘을 기르며 축적한다",
//            "실력과 역량을 기르는 데 집중하는 해입니다. 당장의 성과보다는 장기적인 실력 향상에 투자하세요. 교육이나 훈련을 받으면 나중에 큰 도움이 될 것입니다.", 71));
//
//        TOJEONG_GWA_MAP.put(27, new TojeongGwa("산뢰이", "☶☳", "올바른 양육과 교육의 때",
//            "자신을 기르고 남을 가르치는 일에 좋은 해입니다. 말과 행동을 조심하고 올바른 생활습관을 기르세요. 건강관리와 식생활에 특히 신경 써야 합니다.", 66));
//
//        TOJEONG_GWA_MAP.put(28, new TojeongGwa("택풍대과", "☱☴", "큰 어려움이 있지만 극복 가능하다",
//            "평상시보다 큰 부담이나 어려움이 있을 수 있지만 슬기롭게 헤쳐나갈 수 있습니다. 무리하지 말고 자신의 능력 범위 내에서 차근차근 해결해 나가세요.", 58));
//
//        TOJEONG_GWA_MAP.put(29, new TojeongGwa("감위수", "☵☵", "위험이 거듭되니 매우 조심해야 한다",
//            "어려운 상황이 계속될 수 있으니 신중하게 행동해야 합니다. 모험적인 일은 피하고 안전한 길을 택하세요. 물이나 액체와 관련된 일에는 특별히 주의가 필요합니다.", 45));
//
//        TOJEONG_GWA_MAP.put(30, new TojeongGwa("리위화", "☲☲", "밝음이 계속되어 크게 형통하다",
//            "지혜와 재능이 빛을 발하는 해입니다. 창작활동이나 학술연구에 좋은 성과가 있을 것이며, 유명세를 탈 수도 있습니다. 밝고 긍정적인 마음가짐을 유지하세요.", 79));
//
//        TOJEONG_GWA_MAP.put(31, new TojeongGwa("택산함", "☱☶", "서로 감응하여 좋은 관계를 맺는다",
//            "인간관계에서 좋은 감정의 교류가 있는 해입니다. 이성관계나 결혼에 특히 좋은 시기이며, 사업 파트너십도 잘 이루어질 것입니다. 진실한 마음으로 대하세요.", 77));
//
//        // ===== 중하괘 (中下卦) - 평 (25-39점) =====
//        TOJEONG_GWA_MAP.put(32, new TojeongGwa("뢰풍항", "☳☴", "꾸준히 지속하면 성공한다",
//            "꾸준함과 지속성이 중요한 해입니다. 작은 성과에 만족하지 말고 끝까지 밀고 나가세요. 중도에 포기하지 않는 의지력이 성공의 열쇠가 될 것입니다.", 64));
//
//        TOJEONG_GWA_MAP.put(33, new TojeongGwa("천산둔", "☰☶", "물러나서 때를 기다린다",
//            "적극적으로 나서지 말고 물러나서 때를 기다리는 것이 좋습니다. 무리한 추진보다는 현상유지에 만족하고 실력을 기르는 시간으로 활용하세요.", 52));
//
//        TOJEONG_GWA_MAP.put(34, new TojeongGwa("뢰천대장", "☳☰", "큰 힘을 얻어 크게 발전한다",
//            "강한 의지력과 추진력을 발휘할 수 있는 해입니다. 적극적으로 도전하고 자신의 능력을 마음껏 발휘하세요. 리더십을 발휘할 기회도 많을 것입니다.", 84));
//
//        TOJEONG_GWA_MAP.put(35, new TojeongGwa("화지진", "☲☷", "밝은 빛이 땅 위에 비친다",
//            "자신의 재능이 세상에 알려지는 해입니다. 승진이나 인정을 받을 기회가 많고, 사회적 지위도 향상될 것입니다. 겸손함을 잃지 않도록 주의하세요.", 82));
//
//        TOJEONG_GWA_MAP.put(36, new TojeongGwa("지화명이", "☷☲", "밝음이 상처받는 어려운 시기",
//            "어려운 상황에 처할 수 있지만 내면의 밝음을 잃지 마세요. 겉으로는 힘들어 보여도 마음의 평정을 유지하면 결국 극복할 수 있습니다. 건강관리에 특히 신경 쓰세요.", 48));
//
//        TOJEONG_GWA_MAP.put(37, new TojeongGwa("풍화가인", "☴☲", "집안이 화목하고 가족이 단합한다",
//            "가정이나 조직 내부의 화합이 이루어지는 해입니다. 가족관계가 좋아지고 직장 동료들과의 관계도 원만해질 것입니다. 집안일이나 내부 정리에 신경 쓰세요.", 73));
//
//        TOJEONG_GWA_MAP.put(38, new TojeongGwa("화택규", "☲☱", "서로 등지고 있지만 작은 일은 형통하다",
//            "큰 일보다는 작은 일에 집중하는 것이 좋은 해입니다. 의견 차이나 갈등이 있을 수 있지만 작은 부분에서는 협력이 가능합니다. 너무 큰 욕심을 부리지 마세요.", 56));
//
//        TOJEONG_GWA_MAP.put(39, new TojeongGwa("수산건", "☵☶", "어려움에 막혀 있지만 도움을 받는다",
//            "어려운 상황이지만 주변의 도움을 받아 해결할 수 있습니다. 혼자 해결하려 하지 말고 도움을 요청하세요. 겸손한 자세로 조언을 구하면 좋은 해답을 얻을 것입니다.", 61));
//
//        // ===== 하괘 (下卦) - 흉 (10-24점) =====
//        TOJEONG_GWA_MAP.put(40, new TojeongGwa("뢰수해", "☳☵", "얽힌 것이 풀리고 해방된다",
//            "복잡하게 얽혔던 문제들이 서서히 해결되는 해입니다. 답답했던 상황에서 벗어날 수 있고, 새로운 전기를 마련할 수 있습니다. 과거에 얽매이지 말고 앞으로 나아가세요.", 67));
//
//        TOJEONG_GWA_MAP.put(41, new TojeongGwa("산택손", "☶☱", "줄이고 덜어내어 검소함을 지킨다",
//            "욕심을 줄이고 불필요한 것들을 정리하는 해입니다. 검소하고 절약하는 생활이 나중에 큰 도움이 될 것입니다. 물질적 풍요보다는 정신적 만족을 추구하세요.", 59));
//
//        TOJEONG_GWA_MAP.put(42, new TojeongGwa("풍뢰익", "☴☳", "더하고 늘려서 이익을 얻는다",
//            "노력한 만큼 성과를 거둘 수 있는 해입니다. 투자나 확장에 좋은 시기이며, 새로운 분야에 도전해볼 만합니다. 다른 사람을 도우면 그 덕이 자신에게도 돌아올 것입니다.", 76));
//
//        TOJEONG_GWA_MAP.put(43, new TojeongGwa("택천쾌", "☱☰", "결단력 있게 행동하면 형통하다",
//            "과감한 결단력이 필요한 해입니다. 망설이지 말고 옳다고 생각하는 일은 추진하세요. 다만 독단적이 되지 말고 주변의 의견도 들어보는 것이 좋습니다.", 71));
//
//        TOJEONG_GWA_MAP.put(44, new TojeongGwa("천풍구", "☰☴", "뜻밖의 만남이나 기회가 온다",
//            "예상치 못한 만남이나 기회가 찾아올 수 있습니다. 새로운 인연에 열린 마음으로 임하되, 너무 성급하게 판단하지는 마세요. 신중하면서도 유연한 자세가 필요합니다.", 63));
//
//        TOJEONG_GWA_MAP.put(45, new TojeongGwa("택지취", "☱☷", "모여서 힘을 합친다",
//            "여러 사람이 모여 힘을 합치는 일에 좋은 해입니다. 단체 활동이나 조직적인 일에서 성과를 거둘 것입니다. 리더십을 발휘하거나 팀워크를 중시하는 자세가 중요합니다.", 78));
//
//        TOJEONG_GWA_MAP.put(46, new TojeongGwa("지풍승", "☷☴", "점점 상승하여 높은 곳에 이른다",
//            "서서히 상승하는 운세로 꾸준한 발전이 있을 것입니다. 급하게 서두르지 말고 단계적으로 올라가세요. 목표를 높게 설정하고 차근차근 노력하면 반드시 성취할 수 있습니다.", 80));
//
//        TOJEONG_GWA_MAP.put(47, new TojeongGwa("택수곤", "☱☵", "어려움에 곤궁하지만 의지를 잃지 않는다",
//            "일시적으로 어려운 상황에 처할 수 있지만 포기하지 마세요. 현재의 고난은 미래의 성공을 위한 시련입니다. 의지력을 잃지 않고 끝까지 버티면 반드시 기회가 올 것입니다.", 54));
//
//        TOJEONG_GWA_MAP.put(48, new TojeongGwa("수풍정", "☵☴", "우물을 파서 맑은 물을 얻는다",
//            "깊이 있는 학문이나 기술을 연마하는 데 좋은 해입니다. 표면적인 것보다는 본질을 파고드는 노력이 필요합니다. 전문성을 기르면 나중에 큰 도움이 될 것입니다.", 68));
//
//        TOJEONG_GWA_MAP.put(49, new TojeongGwa("택화혁", "☱☲", "변혁과 개혁의 시기가 온다",
//            "큰 변화와 개혁이 필요한 해입니다. 기존의 방식을 과감히 바꾸고 새로운 시도를 해보세요. 변화에 대한 두려움을 버리고 적극적으로 개혁에 나서면 좋은 결과가 있을 것입니다.", 75));
//
//        TOJEONG_GWA_MAP.put(50, new TojeongGwa("화풍정", "☲☴", "솥처럼 안정되고 조화롭다",
//            "안정되고 조화로운 상태를 유지할 수 있는 해입니다. 가정이나 사업이 안정되고 수입도 꾸준할 것입니다. 전통적인 방법을 고수하면서 점진적인 발전을 추구하세요.", 81));
//
//        TOJEONG_GWA_MAP.put(51, new TojeongGwa("진위뢰", "☳☳", "우뢰가 크게 울려 놀라게 한다",
//            "갑작스런 변화나 충격적인 사건이 있을 수 있습니다. 처음에는 당황스럽겠지만 이를 계기로 새로운 전기를 마련할 수 있습니다. 변화에 잘 적응하는 것이 중요합니다.", 62));
//
//        TOJEONG_GWA_MAP.put(52, new TojeongGwa("간위산", "☶☶", "산처럼 그치고 멈춰서 때를 기다린다",
//            "무리하게 앞으로 나아가지 말고 현 상황에서 머물며 실력을 기르는 시기입니다. 급한 마음을 버리고 차분하게 준비하세요. 때가 되면 자연스럽게 기회가 올 것입니다.", 57));
//
//        TOJEONG_GWA_MAP.put(53, new TojeongGwa("풍산점", "☴☶", "점진적으로 천천히 나아간다",
//            "서두르지 말고 단계적으로 발전시켜 나가는 것이 좋습니다. 큰 성취보다는 작은 진보를 축적하는 데 집중하세요. 꾸준한 노력이 결국 큰 성과로 이어질 것입니다.", 69));
//
//        TOJEONG_GWA_MAP.put(54, new TojeongGwa("뢰택귀매", "☳☱", "귀한 여인이 시집가는 길한 때",
//            "새로운 인연이나 결합이 이루어지는 해입니다. 결혼이나 사업 파트너십에 좋은 시기이며, 상호 보완적인 관계를 맺을 수 있습니다. 서로를 존중하는 마음이 중요합니다.", 74));
//
//        TOJEONG_GWA_MAP.put(55, new TojeongGwa("뢰화풍", "☳☲", "번영과 풍요가 절정에 이른다",
//            "모든 면에서 풍요롭고 번영하는 최고의 해입니다. 사업이나 재물운이 크게 상승하고 명예도 얻을 수 있습니다. 다만 절정의 때이므로 겸손함을 잃지 않도록 주의해야 합니다.", 92));
//
//        TOJEONG_GWA_MAP.put(56, new TojeongGwa("화산려", "☲☶", "나그네처럼 떠돌지만 예의를 지킨다",
//            "이동이나 변화가 많은 해입니다. 출장이나 이사, 전직 등이 있을 수 있지만 나쁘지 않은 결과를 가져다줄 것입니다. 어디를 가든 예의와 겸손함을 잃지 마세요.", 65));
//
//        TOJEONG_GWA_MAP.put(57, new TojeongGwa("손위풍", "☴☴", "부드러운 바람처럼 은근히 스며든다",
//            "부드럽고 온화한 방법으로 목표를 달성하는 해입니다. 강압적이지 말고 설득과 협조로 일을 처리하세요. 여성의 도움이나 부드러운 리더십이 효과적일 것입니다.", 71));
//
//        TOJEONG_GWA_MAP.put(58, new TojeongGwa("태위택", "☱☱", "기쁨과 즐거움이 가득한 때",
//            "기쁘고 즐거운 일들이 많이 생기는 해입니다. 사교활동이나 문화생활을 통해 즐거움을 찾을 수 있고, 좋은 인연도 만날 수 있습니다. 다만 과도한 쾌락은 피하세요.", 83));
//
//        TOJEONG_GWA_MAP.put(59, new TojeongGwa("풍수환", "☴☵", "흩어진 것들이 다시 모인다",
//            "흩어지고 분산되었던 것들이 다시 모이는 해입니다. 잃었던 것을 되찾거나 헤어졌던 사람과 재회할 수 있습니다. 단합과 협력이 중요한 시기입니다.", 66));
//
//        TOJEONG_GWA_MAP.put(60, new TojeongGwa("수택절", "☵☱", "절제하고 제한하여 질서를 세운다",
//            "절제와 자제력이 필요한 해입니다. 욕심을 부리지 말고 적당한 선에서 만족하세요. 규칙과 원칙을 지키면서 질서 있게 생활하는 것이 중요합니다.", 58));
//
//        TOJEONG_GWA_MAP.put(61, new TojeongGwa("풍택중부", "☴☱", "진실한 마음으로 신의를 지킨다",
//            "진실하고 성실한 마음가짐이 중요한 해입니다. 약속을 지키고 신뢰를 쌓으면 좋은 결과가 있을 것입니다. 작은 일에도 정성을 다하는 자세가 필요합니다.", 77));
//
//        TOJEONG_GWA_MAP.put(62, new TojeongGwa("뢰산소과", "☳☶", "작은 것에서 지나침이 있다",
//            "작은 일에는 적극적이어도 좋지만 큰 일에는 신중해야 하는 해입니다. 세부사항에 신경 쓰되 전체적인 균형을 잃지 마세요. 완벽주의가 오히려 독이 될 수 있습니다.", 60));
//
//        TOJEONG_GWA_MAP.put(63, new TojeongGwa("수화기제", "☵☲", "이미 완성되었지만 계속 노력해야 한다",
//            "목표를 달성했지만 방심하지 말고 계속 노력해야 하는 해입니다. 현재의 성취에 안주하지 말고 더 높은 목표를 설정하세요. 완성 후의 관리가 더욱 중요합니다.", 73));
//
//        TOJEONG_GWA_MAP.put(64, new TojeongGwa("화수미제", "☲☵", "아직 완성되지 않았으니 계속 정진한다",
//            "아직 완성되지 않은 상태이므로 계속 노력해야 하는 해입니다. 포기하지 말고 끝까지 최선을 다하세요. 목표 달성까지 얼마 남지 않았으니 조금만 더 인내하면 성공할 것입니다.", 68));
//
//        // ===== 하하괘 (下下卦) - 대흉 (0-9점) =====
//        // 64괘 중에서는 특별히 대흉한 괘는 없으며, 모든 괘에는 나름의 의미와 교훈이 있습니다.
//        // 가장 어려운 괘들도 최소 40점 이상의 의미를 가지고 있습니다.
//    }



}
