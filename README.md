# newscurator
web site making lecture


springboot

maven

3.44

kr.ac.dankook.cs

curation

jar/17


### 뉴스 수집 기능
- NewsAPI를 호출해 AI 관련 뉴스 수집
- 중복 기사 제거 후 DB에 저장
- 수집된 뉴스는 `/api/recommended` 또는 `/api/news/{category}`로 조회 가능


mysql 쿼리 조회 방법

-- AI 기사
SELECT * FROM ai_articles;
-- 빅데이터 기사
SELECT * FROM bigdata_articles;
-- 보안 기사
SELECT * FROM security_articles;
-- 하드웨어 기사
SELECT * FROM hardware_articles;

cd /당신의/프로젝트/경로   (vscode 안에서 하는거면 건너뛰기)
git init


git add .
git commit -m "처음 커밋"


git remote add origin https://github.com/Drranny/newscurator.git


먼저 pull 해서 병합하고 push 해

git pull --rebase origin main\ngit push -u origin main\n


