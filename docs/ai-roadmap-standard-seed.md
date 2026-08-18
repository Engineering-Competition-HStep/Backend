# AI 직무별 표준 로드맵 자동 초기화

## 목적

서버가 시작될 때 현재 `JOB` 테이블에 등록된 모든 직무를 조회하고,
각 직무에 필요한 AI 표준 로드맵 항목이 없으면 자동으로 추가한다.

현재 `JobSeedCatalog` 기준 직무뿐 아니라 관리자가 DB에 별도로 추가한 직무도
`job_category`가 정상적으로 등록되어 있으면 동일한 방식으로 표준 로드맵이 생성된다.

## 실행 순서

1. `TrackDataInitializer`
2. `JobDataInitializer` (`ApplicationReadyEvent`, `@Order(200)`)
3. `AiRoadmapStandardItemDataInitializer` (`ApplicationReadyEvent`, `@Order(300)`)

따라서 트랙과 직무 데이터가 먼저 저장된 뒤 표준 로드맵이 생성된다.

## 직무당 생성 항목

각 직무에 총 12개 템플릿을 사용한다.

| 학년 | 항목 | 분류 | 필수 여부 |
|---|---|---|---|
| 2 | 기초 역량 학습 | COURSE | 필수 |
| 2 | 직무·산업 리서치 | ETC | 필수 |
| 2 | 기초 미니 프로젝트 | PROJECT | 선택 |
| 3 | 핵심 실무 역량 강화 | COURSE | 필수 |
| 3 | 포트폴리오 프로젝트 | PROJECT | 필수 |
| 3 | 심화 프로젝트 확장 | PROJECT | 선택 |
| 3 | 관련 자격증 준비 | CERTIFICATE | 선택 |
| 3 | 인턴·현장실습 준비 | INTERNSHIP | 필수 |
| 3 | 공모전·대외활동 참여 | CONTEST | 선택 |
| 4 | 포트폴리오 완성도 개선 | PROJECT | 필수 |
| 4 | 지원서·채용 공고 분석 | ETC | 필수 |
| 4 | 면접·실무 과제 대비 | ETC | 필수 |

설명, 키워드, 프로젝트 주제와 실무 경험 내용은 `JobCategory` 16종에 따라 다르게 생성된다.

## 중복 방지

자동 생성 항목에는 직무별 `seed_key`를 저장한다.

- `(job_id, seed_key)`는 유일하다.
- 같은 `seed_key`가 있으면 기존 데이터를 유지한다.
- 과거에 같은 직무·학년·카테고리·제목으로 수동 삽입한 행이 있으면
  새 행을 만들지 않고 기존 행에 `seed_key`만 지정한다.
- 관리자 API로 생성한 `seed_key IS NULL` 항목은 삭제하거나 덮어쓰지 않는다.
- 자동 생성 항목의 제목이나 설명을 관리자가 수정해도 `seed_key`가 유지되므로
  서버 재시작 시 원본 내용으로 되돌리지 않는다.
- 자동 생성 항목을 삭제하면 다음 서버 시작 시 다시 생성된다.

## 설정

기본값은 활성화 상태이다.

```properties
app.ai-roadmap-standard-seed.enabled=${AI_ROADMAP_STANDARD_SEED_ENABLED:true}
```

일시적으로 비활성화하려면:

```properties
app.ai-roadmap-standard-seed.enabled=false
```

또는 환경 변수:

```text
AI_ROADMAP_STANDARD_SEED_ENABLED=false
```

## 확인 SQL

```sql
SELECT
    COUNT(*) AS job_count
FROM job;

SELECT
    COUNT(*) AS seeded_standard_item_count
FROM ai_roadmap_standard_item
WHERE seed_key IS NOT NULL;

SELECT
    j.job_id,
    j.job_name,
    COUNT(s.standard_item_id) AS item_count
FROM job j
LEFT JOIN ai_roadmap_standard_item s
       ON s.job_id = j.job_id
GROUP BY j.job_id, j.job_name
HAVING COUNT(s.standard_item_id) < 12
ORDER BY j.job_name;

SELECT
    j.job_id,
    j.job_name,
    s.seed_key,
    s.target_grade,
    s.category,
    s.priority,
    s.required_item,
    s.title
FROM job j
JOIN ai_roadmap_standard_item s
     ON s.job_id = j.job_id
WHERE j.job_name = '백엔드 개발자'
ORDER BY s.target_grade, s.display_order;
```

`HAVING COUNT(s.standard_item_id) < 12` 결과가 없으면 모든 직무에 최소 12개의 표준 항목이 존재한다.
관리자가 별도 항목을 추가한 직무는 12개보다 많을 수 있다.
