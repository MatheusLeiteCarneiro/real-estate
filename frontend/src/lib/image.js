const CLOUDINARY_HOST = "res.cloudinary.com"
const UPLOAD_SEGMENT = "/image/upload/"

export const IMAGE_WIDTH = {
  thumbnail: 240,
  card: 800,
  photo: 1600,
  fullscreen: 2400,
}

export function resizeImage(url, width) {
  if (!url?.includes(CLOUDINARY_HOST) || !url.includes(UPLOAD_SEGMENT)) return url
  return url.replace(UPLOAD_SEGMENT, `${UPLOAD_SEGMENT}w_${width},c_limit,f_auto,q_auto/`)
}
