#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import matplotlib.pyplot as plt
import matplotlib.patches as patches
from matplotlib.patches import FancyBboxPatch
import matplotlib.colors as mcolors

# 定义颜色
COLORS = {
    'frontend': '#4ECDC4',
    'security': '#45B7D1',
    'service': '#96CEB4',
    'data_access': '#FFEAA7',
    'data_storage': '#DDA0DD',
    'module': '#F8C471',
    'text': '#2C3E50'
}

def create_architecture_image():
    # 创建图形和轴
    fig, ax = plt.subplots(figsize=(16, 12), dpi=100)

    # 设置背景为白色
    fig.patch.set_facecolor('white')
    ax.set_facecolor('white')

    # 隐藏坐标轴
    ax.axis('off')

    # 设置图形边界
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)

    # 定义层级位置
    levels = {
        'frontend': 90,
        'security': 75,
        'service': 55,
        'data_access': 35,
        'data_storage': 15
    }

    # 定义层级高度
    level_height = 12

    # 绘制前端展示层 (使用英文)
    draw_level_box(ax, 10, levels['frontend'], 80, level_height, COLORS['frontend'],
                   'Frontend Layer', 'Vue + Element UI + bpmn-js + Vue Router + Vuex + Axios')

    # 绘制安全认证层 (使用英文)
    draw_level_box(ax, 10, levels['security'], 80, level_height, COLORS['security'],
                   'Security Layer', 'Sa-Token (JWT Authentication)')

    # 绘制应用服务层
    draw_level_box(ax, 10, levels['service'], 80, level_height, COLORS['service'],
                   'Service Layer', '', False)

    # 应用服务层模块 (使用英文)
    modules = [
        {'x': 15, 'y': levels['service'] + 2, 'w': 15, 'h': 5, 'title': 'System Mgmt', 'subtitle': '(crm-system)'},
        {'x': 35, 'y': levels['service'] + 2, 'w': 15, 'h': 5, 'title': 'Workflow', 'subtitle': '(crm-flowable)'},
        {'x': 55, 'y': levels['service'] + 2, 'w': 15, 'h': 5, 'title': 'Object Storage', 'subtitle': '(crm-oss)'},
        {'x': 75, 'y': levels['service'] + 2, 'w': 10, 'h': 5, 'title': 'SMS Service', 'subtitle': '(crm-sms)'},
        {'x': 15, 'y': levels['service'] - 5, 'w': 15, 'h': 5, 'title': 'Code Gen', 'subtitle': '(crm-generator)'},
        {'x': 35, 'y': levels['service'] - 5, 'w': 15, 'h': 5, 'title': 'Core Framework', 'subtitle': '(crm-framework)'},
        {'x': 55, 'y': levels['service'] - 5, 'w': 15, 'h': 5, 'title': 'Common Utils', 'subtitle': '(crm-common)'}
    ]

    for module in modules:
        draw_module_box(ax, module['x'], module['y'], module['w'], module['h'], COLORS['module'],
                       module['title'], module['subtitle'])

    # 绘制数据访问层 (使用英文)
    draw_level_box(ax, 10, levels['data_access'], 80, level_height, COLORS['data_access'],
                   'Data Access Layer', 'MyBatis-Plus + Multi-Datasource')

    # 绘制数据存储层
    draw_level_box(ax, 10, levels['data_storage'], 80, level_height, COLORS['data_storage'],
                   'Storage Layer', '', False)

    # 数据存储层模块 (使用英文)
    storage_modules = [
        {'x': 20, 'y': levels['data_storage'] - 1, 'w': 15, 'h': 5, 'title': 'MySQL', 'subtitle': '(Relational DB)'},
        {'x': 45, 'y': levels['data_storage'] - 1, 'w': 15, 'h': 5, 'title': 'Redis', 'subtitle': '(Cache Service)'},
        {'x': 70, 'y': levels['data_storage'] - 1, 'w': 15, 'h': 5, 'title': 'Cloud Storage', 'subtitle': '(OSS/S3)'}
    ]

    for module in storage_modules:
        draw_module_box(ax, module['x'], module['y'], module['w'], module['h'], '#BA55D3',
                       module['title'], module['subtitle'])

    # 绘制连接线
    draw_connections(ax, levels, level_height)

    # 设置标题 (使用英文)
    plt.title('zqzt_backend Architecture', fontsize=24, pad=30, color=COLORS['text'])

    # 保存图片
    plt.tight_layout()
    plt.savefig('/Users/liutong/farben/ruoyi-flowable-plus-0.8.X/docs/系统架构图.png', dpi=300, bbox_inches='tight')

    print("架构图图片已生成: /Users/liutong/farben/ruoyi-flowable-plus-0.8.X/docs/系统架构图.png")

def draw_level_box(ax, x, y, width, height, color, title, subtitle, draw_subtitle=True):
    # 绘制圆角矩形
    box = FancyBboxPatch(
        (x, y - height/2), width, height,
        boxstyle="round,pad=0.1",
        facecolor=color,
        alpha=0.9,
        edgecolor='black',
        linewidth=1
    )
    ax.add_patch(box)

    # 添加标题 - 使用默认字体
    ax.text(x + width/2, y + 1, title, fontsize=14, fontweight='bold',
            ha='center', va='center', color=COLORS['text'], fontfamily='sans-serif')

    # 添加副标题 - 使用默认字体
    if draw_subtitle and subtitle:
        ax.text(x + width/2, y - 1, subtitle, fontsize=10,
                ha='center', va='center', color=COLORS['text'], fontfamily='sans-serif')

def draw_module_box(ax, x, y, width, height, color, title, subtitle):
    # 绘制圆角矩形
    box = FancyBboxPatch(
        (x, y - height/2), width, height,
        boxstyle="round,pad=0.05",
        facecolor=color,
        alpha=0.8,
        edgecolor='black',
        linewidth=0.8
    )
    ax.add_patch(box)

    # 添加标题和副标题 - 使用默认字体
    ax.text(x + width/2, y + 0.5, title, fontsize=10, fontweight='bold',
            ha='center', va='center', color=COLORS['text'], fontfamily='sans-serif')
    ax.text(x + width/2, y - 0.5, subtitle, fontsize=8,
            ha='center', va='center', color=COLORS['text'], fontfamily='sans-serif')

def draw_connections(ax, levels, level_height):
    # 绘制层级之间的连接线
    y_positions = list(levels.values())

    for i in range(len(y_positions) - 1):
        y1 = y_positions[i] - level_height/2
        y2 = y_positions[i + 1] + level_height/2

        # 垂直线
        ax.plot([50, 50], [y1, y2], color='black', linewidth=1.5, linestyle='-')

        # 箭头 - 简化箭头以避免显示问题
        ax.arrow(50, y2, 0, 1, head_width=1, head_length=0.8, fc='black', ec='black')

if __name__ == "__main__":
    create_architecture_image()
