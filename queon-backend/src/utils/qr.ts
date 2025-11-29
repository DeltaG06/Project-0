import QRCode from "qrcode";

export async function generateQrDataUrl(payload: object): Promise<string> {
  const text = JSON.stringify(payload);

  // This returns: "data:image/png;base64,...."
  const dataUrl = await QRCode.toDataURL(text, {
    errorCorrectionLevel: "M",
    width: 300,
  });

  return dataUrl;
}
