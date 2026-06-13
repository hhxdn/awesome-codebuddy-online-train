#!/usr/bin/env python3
"""
Generate Chinese-style 结业证书 (Certificate of Completion) images using Pillow.

Usage:
    python3 generate_cert.py --output /tmp/certs --student "张三" --phone "13800001001" \
        --courses "Spring Boot 入门到精通,Spring Cloud 微服务实战"

Output:
    One PNG file per course, named <course_index>.png in the output directory.
"""

import argparse
import os
import sys
import textwrap
from datetime import datetime
from io import BytesIO

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("ERROR: Pillow not installed. Run: pip3 install Pillow")
    sys.exit(1)

# Certificate dimensions (A4-like ratio)
W, H = 800, 566

# Color scheme
BG_COLOR = "#FFFDF5"
BORDER_OUTER = "#C8963E"
BORDER_INNER = "#D4A84B"
TITLE_COLOR = "#8B4513"
TEXT_COLOR = "#333"
INFO_COLOR = "#555"
SEAL_COLOR = "#C41E3A"
FOOTER_COLOR = "#999"
DECOR_LINE = "#E8D5B7"


def _get_font(size: int) -> ImageFont.FreeTypeFont:
    """Try to load a CJK-capable font, fall back to default."""
    font_paths = [
        "/System/Library/Fonts/STHeiti Light.ttc",  # macOS
        "/System/Library/Fonts/PingFang.ttc",       # macOS
        "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",  # Linux
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",  # Linux
    ]
    for fp in font_paths:
        if os.path.exists(fp):
            return ImageFont.truetype(fp, size)
    return ImageFont.load_default()


def draw_decorative_corners(draw: ImageDraw.Draw):
    """Draw ornamental arcs at the four corners."""
    for x, y in [(30, 30), (W - 30, 30), (30, H - 30), (W - 30, H - 30)]:
        for i in range(3):
            r = 50 - i * 10
            draw.arc([x - r, y - r, x + r, y + r], 0, 90 * draw.im.width // W,
                     fill=BORDER_OUTER, width=2)


def draw_seal(draw: ImageDraw.Draw, cx: int, cy: int, font: ImageFont.FreeTypeFont):
    """Draw a red circular seal with rotated text."""
    r = 42
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=SEAL_COLOR, width=4)
    # Top line
    bbox = draw.textbbox((0, 0), "在线学习", font=font)
    tw = bbox[2] - bbox[0]
    draw.text((cx - tw // 2, cy - 10), "在线学习", fill=SEAL_COLOR, font=font)
    # Bottom line
    bbox = draw.textbbox((0, 0), "证书专用章", font=font)
    tw = bbox[2] - bbox[0]
    draw.text((cx - tw // 2, cy + 8), "证书专用章", fill=SEAL_COLOR, font=font)


def generate_certificate(student_name: str, phone: str, course_name: str,
                         course_id: int, output_path: str) -> str:
    """
    Generate a single certificate image.

    Args:
        student_name: Student's real name (e.g., "张三")
        phone: Student's phone number
        course_name: Course display name
        course_id: Course ID (used in cert number)
        output_path: Where to save the PNG file

    Returns:
        Path to the generated PNG file
    """
    img = Image.new("RGB", (W, H), BG_COLOR)
    draw = ImageDraw.Draw(img)

    # Borders
    draw.rectangle([16, 16, W - 17, H - 17], outline=BORDER_OUTER, width=8)
    draw.rectangle([30, 30, W - 31, H - 31], outline=BORDER_INNER, width=2)

    # Decorative corners
    draw_decorative_corners(draw)

    # Fonts
    ft = _get_font(36)   # title
    fm = _get_font(18)   # body
    fs = _get_font(16)   # info
    fx = _get_font(12)   # footer
    fseal = _get_font(13)  # seal

    # Title
    title = "结业证书"
    bbox = draw.textbbox((0, 0), title, font=ft)
    tw = bbox[2] - bbox[0]
    draw.text(((W - tw) // 2, 80), title, fill=TITLE_COLOR, font=ft)
    draw.line([(W // 2 - 100, 130), (W // 2 + 100, 130)], fill=BORDER_OUTER, width=2)

    # Body text
    text = f"兹证明 {student_name}（{phone}）已完成《{course_name}》课程学习，成绩合格，准予结业。"
    lines = textwrap.wrap(text, width=26)
    y = 170
    for line in lines:
        bbox = draw.textbbox((0, 0), line, font=fm)
        lw = bbox[2] - bbox[0]
        draw.text(((W - lw) // 2, y), line, fill=TEXT_COLOR, font=fm)
        y += 38

    # Separator
    y += 10
    draw.line([(120, y), (W - 120, y)], fill=DECOR_LINE, width=1)
    y += 30

    # Info section
    now = datetime.now()
    cert_no = f"CERT-{now.strftime('%Y%m%d%H%M%S')}-5-{course_id}"

    info_lines = [
        f"证书编号：{cert_no}",
        f"课程名称：{course_name}",
        f"颁发时间：{now.strftime('%Y-%m-%d')}",
    ]
    for line in info_lines:
        draw.text((120, y), line, fill=INFO_COLOR, font=fs)
        y += 30

    # Seal (positioned at bottom-right of info area)
    sx, sy = W - 150, y - 50
    draw_seal(draw, sx, sy, fseal)

    # Footer
    draw.text((W // 2, H - 40), "本证书由在线学习平台颁发，可通过证书编号查验真伪",
              fill=FOOTER_COLOR, font=fx, anchor="mt")

    # Save
    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)
    img.save(output_path, "PNG")
    return output_path


def main():
    parser = argparse.ArgumentParser(
        description="Generate certificate of completion images"
    )
    parser.add_argument("--output", "-o", required=True,
                        help="Output directory for certificate images")
    parser.add_argument("--student", "-s", required=True,
                        help="Student real name")
    parser.add_argument("--phone", "-p", required=True,
                        help="Student phone number")
    parser.add_argument("--courses", "-c", required=True,
                        help="Comma-separated list: 'name:id,name:id' or just 'name,name' "
                             "(use 1-based index if no ID provided)")
    args = parser.parse_args()

    course_entries = [c.strip() for c in args.courses.split(",") if c.strip()]
    print(f"Generating {len(course_entries)} certificate(s) for {args.student} ({args.phone})")

    for i, entry in enumerate(course_entries):
        if ":" in entry:
            name, cid = entry.rsplit(":", 1)
            cid = int(cid)
        else:
            name = entry
            cid = i + 1

        path = os.path.join(args.output, f"{cid}.png")
        generate_certificate(args.student, args.phone, name, cid, path)
        print(f"  [{i + 1}/{len(course_entries)}] {name} -> {path}")

    print("Done.")


if __name__ == "__main__":
    main()
