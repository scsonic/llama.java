import wave
import struct

# 设置参数
channels = 1  # 声道数
sample_width = 2  # 采样位宽（字节数）
frame_rate = 44100  # 采样频率
duration = 3  # 声音时长（秒）

# 创建 WAV 文件
with wave.open("empty.wav", "w") as wf:
    wf.setnchannels(channels)
    wf.setsampwidth(sample_width)
    wf.setframerate(frame_rate)
    wf.setnframes(duration * frame_rate)

    # 写入空数据
    for _ in range(duration * frame_rate):
        wf.writeframes(struct.pack("<h", 0))
