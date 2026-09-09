export type UserView = {
  id: number; username: string; status: string; nickname?: string; avatarUrl?: string;
  gender?: string; lockTime?: string; createdTime?: string; updatedTime?: string; authorities?: string[];
};

export type LoginResponse = { userId: number; username: string; authorities: string[] };

export type LoginRequest = { username: string; password: string };
