import request from '../../../utils/request';

/**
 * 个人信息VO
 */
export interface ProfileVO {
  id: number;
  username: string;
  nickname: string;
  email?: string;
  mobile?: string;
  sex?: number;
  avatar?: string;
}

/**
 * 个人信息更新DTO
 */
export interface ProfileUpdateDTO {
  nickname: string;
  email?: string;
  mobile?: string;
  sex?: number;
  avatar?: string;
}

/**
 * 密码更新DTO
 */
export interface PasswordUpdateDTO {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * 个人信息API
 */
export const profileApi = {
  /**
   * 获取当前用户个人信息
   */
  getProfile: (): Promise<ProfileVO> => {
    return request.get('/system/user/profile');
  },

  /**
   * 更新当前用户个人信息
   */
  updateProfile: (data: ProfileUpdateDTO): Promise<void> => {
    return request.put('/system/user/profile', data);
  },

  /**
   * 修改当前用户密码
   */
  updatePassword: (data: PasswordUpdateDTO): Promise<void> => {
    return request.put('/system/user/update-password', data);
  },
};

export default profileApi;

