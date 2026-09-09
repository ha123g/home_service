export type CurrentUser = {
  id?: number;
  username?: string;
  status?: string;
  authorities?: string[];
  roles?: string[];
};

export const authorityOf = (user: CurrentUser | undefined) => [
  ...(user?.authorities ?? []),
  ...(user?.roles ?? []),
];

export const hasAnyRole = (user: CurrentUser | undefined, roles: string[]) => {
  const authorities = authorityOf(user);
  return roles.some((role) => authorities.includes(role) || authorities.includes(`ROLE_${role}`));
};

export default (initialState: { currentUser?: CurrentUser }) => {
  const user = initialState?.currentUser;
  return {
    isLoggedIn: Boolean(user?.id),
    isAdmin: hasAnyRole(user, [
      'username_super_admin', 'username_sec_admin', 'username_aud_admin', 'username_sys_admin',
    ]),
    isPlatformUser: hasAnyRole(user, ['username_pla_user']),
  };
};
