package kr.co.mayfarm.plugin.analysis.enums;

import java.util.Arrays;

public enum PosType {

    NNG("보통 명사"),
    NNP("고유 명사"),
    NNB("일반 의존 명사"),
    NNM("단위 의존 명사"),
    NR("수사"),
    NP("대명사"),
    VV("동사"),
    VA("형용사"),
    VXV("보조 동사"),
    VXA("보조 형용사"),
    VCP("긍정 지정사, 서술격 조사 '이다'"),
    VCN("부정 지정사, 형용사 '아니다'"),
    MDT("일반 관형사"),
    MDN("수 관형사"),
    MAG("일반 부사"),
    MAC("접속 부사"),
    IC("감탄사"),
    JKS("주격 조사"),
    JKC("보격 조사"),
    JKG("관형격 조사"),
    JKO("목적격 조사"),
    JKM("부사격 조사"),
    JKI("호격 조사"),
    JKQ("인용격 조사"),
    JX("보조사"),
    JC("접속 조사"),
    EPH("존칭 선어말 어미"),
    EPT("시제 선어말 어미"),
    EPP("공손 선어말 어미"),
    EFN("평서형 종결 어미"),
    EFQ("의문형 종결 어미"),
    EFO("명령형 종결 어미"),
    EFA("청유형 종결 어미"),
    EFI("감탄형 종결 어미"),
    EFR("존칭형 종결 어미"),
    ECE("대등 연결 어미"),
    ECD("의존적 연결 어미"),
    ECS("보조적 연결 어미"),
    ETN("명사형 전성 어미"),
    ETD("관형형 전성 어미"),
    XPN("체언 접두사"),
    XPV("용언 접두사"),
    XSN("명사 파생 접미사"),
    XSV("동사 파생 접미사"),
    XSA("형용사 파생 접미사"),
    XR("어근"),
    SF("마침표물음표,느낌표"),
    SP("쉼표,가운뎃점,콜론,빗금"),
    SS("따옴표,괄호표,줄표"),
    SE("줄임표"),
    SO("붙임표(물결,숨김,빠짐)"),
    SW("기타기호 (논리수학기호,화폐기호)"),
    UN("명사추정범주"),
    OL("외국어"),
    OH("한자"),
    ON("숫자"),
    NO_SUCH_TYPE("없는 타입");



    private final String description;
    private final String lowerCase;

    PosType(String description) {
        this.description = description;
        this.lowerCase = toString().toLowerCase();
    }

    public static PosType find(String tag) {
        return Arrays.stream(values())
                .filter(posType -> posType.lowerCase.equals(tag.toLowerCase()))
                .findAny()
                .orElse(NO_SUCH_TYPE);
    }


}