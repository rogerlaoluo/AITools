// 通用工具函数

// API 基础路径
const API_BASE = '/api/tools';

// 获取分类图标
function getCategoryIcon(category) {
    const icons = {
        '编码转换': '🔄',
        '数据处理': '📊',
        '图像生成': '🎨',
        '评估工具': '📈',
        '参考工具': '📖'
    };
    return icons[category] || '🔧';
}

// 执行工具
async function executeTool(toolId, params) {
    try {
        const response = await fetch(`${API_BASE}/${toolId}/execute`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(params)
        });

        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error executing tool:', error);
        return {
            success: false,
            error: '网络错误或服务器无响应'
        };
    }
}

// 获取工具参数
function getToolParams(formId) {
    const form = document.getElementById(formId);
    const inputs = form.querySelectorAll('input, textarea, select');
    const params = {};

    inputs.forEach(input => {
        if (input.type === 'radio') {
            if (input.checked) {
                params[input.name] = input.value;
            }
        } else {
            params[input.name] = input.value;
        }
    });

    return params;
}

// 显示结果
function displayResult(result, contentId = 'resultContent') {
    const resultBox = document.getElementById('resultBox');
    const resultContent = document.getElementById(contentId);

    if (!resultBox || !resultContent) {
        console.error('Result elements not found');
        return;
    }

    resultBox.style.display = 'block';
    resultBox.className = 'result-box';

    if (result.success) {
        resultBox.classList.add('success');
        handleSuccessResult(result, resultContent);
    } else {
        resultBox.classList.add('error');
        resultContent.textContent = result.error || '操作失败';
    }
}

// 处理成功结果
function handleSuccessResult(result, resultContent) {
    // 二维码图片
    if (result.data?.result && result.data.result.startsWith('data:image')) {
        resultContent.innerHTML = `<img src="${result.data.result}" alt="Result" style="max-width: 100%; height: auto; display: block; margin: 0 auto;">`;

        if (result.data.size || result.data.errorCorrection) {
            const info = [];
            if (result.data.size) info.push(`尺寸: ${result.data.size}x${result.data.size}`);
            if (result.data.errorCorrection) info.push(`纠错: ${result.data.errorCorrection}`);
            if (info.length > 0) {
                resultContent.innerHTML += `<p style="text-align: center; margin-top: 10px; color: #666; font-size: 0.9rem;">${info.join(' | ')}</p>`;
            }
        }
    }
    // ASCII 表格
    else if (result.data?.asciiData) {
        renderAsciiTable(result.data.asciiData, resultContent);
    }
    // 续签评估
    else if (result.data?.details && result.data.details['续签评估']) {
        renderContractRenewal(result.data.details, resultContent);
    }
    // 其他结果
    else {
        let resultText = result.data?.result || '操作成功';

        // 如果有额外的信息，显示出来
        if (result.data?.key) {
            resultText += `\n\n密钥: ${result.data.key}`;
        }
        if (result.data?.info) {
            resultText += `\n\n${result.data.info}`;
        }

        resultContent.textContent = resultText;
    }
}

// 渲染 ASCII 表格
function renderAsciiTable(asciiData, container) {
    container.innerHTML = `
        <div style="overflow-x: auto;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.9rem;">
                <thead>
                    <tr style="background: #667eea; color: white;">
                        <th style="padding: 12px; text-align: center; border: 1px solid #5568d3;">DEC</th>
                        <th style="padding: 12px; text-align: center; border: 1px solid #5568d3;">HEX</th>
                        <th style="padding: 12px; text-align: center; border: 1px solid #5568d3;">OCT</th>
                        <th style="padding: 12px; text-align: center; border: 1px solid #5568d3;">字符</th>
                        <th style="padding: 12px; text-align: center; border: 1px solid #5568d3;">类型</th>
                        <th style="padding: 12px; text-align: center; border: 1px solid #5568d3;">转义</th>
                    </tr>
                </thead>
                <tbody>
                    ${asciiData.map((item, idx) => `
                        <tr style="${idx % 2 === 0 ? 'background: #f7fafc;' : 'background: white;'}">
                            <td style="padding: 8px; text-align: center; border: 1px solid #e2e8f0; font-family: monospace; font-weight: bold;">${item.dec}</td>
                            <td style="padding: 8px; text-align: center; border: 1px solid #e2e8f0; font-family: monospace; color: #667eea;">${item.hex}</td>
                            <td style="padding: 8px; text-align: center; border: 1px solid #e2e8f0; font-family: monospace; color: #764ba2;">${item.oct}</td>
                            <td style="padding: 8px; text-align: center; border: 1px solid #e2e8f0; font-family: monospace; font-size: 1.1rem; ${item.type === '控制字符' || item.type === '删除' ? 'color: #f56565;' : ''}">${item.char}</td>
                            <td style="padding: 8px; text-align: center; border: 1px solid #e2e8f0; font-size: 0.85rem; color: #718096;">${item.type}</td>
                            <td style="padding: 8px; text-align: center; border: 1px solid #e2e8f0; font-size: 0.85rem; color: #48bb78;">${item.escape || '-'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
            <p style="text-align: center; margin-top: 15px; color: #666; font-size: 0.9rem;">
                共 ${asciiData.length} 个字符
            </p>
        </div>
    `;
}

// 渲染续签评估结果
function renderContractRenewal(details, container) {
    const score = details['总分'];
    const renewal = details['续签评估'];
    const color = details['颜色标识'];

    let colorClass = '';
    if (color === 'green') colorClass = '#48bb78';
    else if (color === 'blue') colorClass = '#4299e1';
    else if (color === 'orange') colorClass = '#ed8936';
    else if (color === 'red') colorClass = '#f56565';

    container.innerHTML = `
        <div style="text-align: center; padding: 20px;">
            <div style="font-size: 3rem; font-weight: bold; color: ${colorClass}; margin-bottom: 10px;">${score}</div>
            <div style="font-size: 1.5rem; color: ${colorClass}; margin-bottom: 20px; font-weight: 600;">${renewal}</div>
            <div style="text-align: left; background: #f7fafc; padding: 15px; border-radius: 8px; margin-top: 20px;">
                <div style="margin-bottom: 10px;"><strong>职级:</strong> ${details['职级']}级 | <strong>年龄:</strong> ${details['年龄']}岁 | <strong>职级年龄得分:</strong> ${details['职级年龄得分']}</div>
                <div style="margin-bottom: 10px;"><strong>绩效评估得分:</strong> ${details['绩效评估得分']}</div>
                <div style="margin-left: 20px; font-size: 0.9rem; color: #666;">
                    2022: ${details['2022绩效']} | 2023: ${details['2023绩效']} | 2024: ${details['2024绩效']} | 2025: ${details['2025绩效']}
                </div>
                <div style="margin-top: 10px;"><strong>团队业务发展潜力:</strong> ${details['团队业务发展潜力得分']} | <strong>其它因素:</strong> ${details['其它因素得分']}</div>
            </div>
            <div style="text-align: left; background: #fffaf0; padding: 15px; border-radius: 8px; margin-top: 15px; border-left: 4px solid #ed8936;">
                <strong>建议:</strong> ${details['建议']}
            </div>
        </div>
    `;
}

// 清空表单
function clearForm(formId = 'toolForm') {
    const form = document.getElementById(formId);
    if (!form) return;

    const inputs = form.querySelectorAll('input, textarea, select');

    inputs.forEach(input => {
        if (input.type === 'radio') {
            const name = input.name;
            const firstRadio = form.querySelector(`input[name="${name}"]`);
            if (firstRadio) {
                firstRadio.checked = true;
            }
        } else {
            input.value = '';
        }
    });

    const resultBox = document.getElementById('resultBox');
    if (resultBox) {
        resultBox.style.display = 'none';
    }
}
