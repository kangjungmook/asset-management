const ACTION_LABELS = {
  LOGIN: '로그인',
  LOGOUT: '로그아웃',
  USER_CREATE: '사용자 생성',
  USER_UPDATE: '사용자 정보 수정',
  USER_DELETE: '사용자 삭제',
  USER_DEACTIVATE: '사용자 비활성화',
  USER_ACTIVATE: '사용자 활성화',
  ROLE_GRANT: '역할 부여',
  ROLE_REVOKE: '역할 회수',
  PERMISSION_GRANT: '권한 부여',
  PERMISSION_REVOKE: '권한 회수',
  PASSWORD_RESET: '임시 비밀번호 발급',
  PASSWORD_CHANGE: '비밀번호 변경',
  PERMISSION_CREATE: '권한 항목 생성',
  PERMISSION_UPDATE: '권한 항목 수정',
  PERMISSION_DELETE: '권한 항목 삭제',
  PASSWORD_ENTRY_CREATE: '패스워드 관리대장 등록',
  PASSWORD_ENTRY_UPDATE: '패스워드 관리대장 수정',
  PASSWORD_ENTRY_DELETE: '패스워드 관리대장 삭제',
  DEPARTMENT_CREATE: '부서 생성',
  DEPARTMENT_UPDATE: '부서 수정',
  DEPARTMENT_DELETE: '부서 삭제',
  ACCOUNT_TYPE_CREATE: '계정유형 생성',
  ACCOUNT_TYPE_UPDATE: '계정유형 수정',
  ACCOUNT_TYPE_DELETE: '계정유형 삭제',
}

const TARGET_PREFIX_LABELS = {
  user: '사용자',
  perm: '권한',
  dept: '부서',
  type: '계정유형',
  pw: '패스워드 항목',
}

export function actionLabel(action) {
  return ACTION_LABELS[action] || action
}

export function targetLabel(target) {
  if (!target) return '-'
  const [prefix, id] = target.split(':')
  const label = TARGET_PREFIX_LABELS[prefix]
  return label ? `${label} #${id}` : target
}
