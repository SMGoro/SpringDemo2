const sendBtn = document.querySelector('.send-btn');
const textarea = document.querySelector('textarea');
const chatArea = document.getElementById('chat-area');


const now = new Date();
const time = now.getTime();

let name = prompt("请输入用户昵称")

while (true) {
    if (name == null || name == "" || name.length > 10) {
        alert("用户名违法！请重新输入")
        name = prompt("请输入用户昵称")
    } else {
        break;
    }
}

let roomid = prompt("请输入加入房间");

while (true) {
    if (roomid == null || roomid == "") {
        roomid = "public"
        document.getElementById("title").innerHTML = roomid;
    } else {
        document.getElementById("title").innerHTML = roomid;
        break;
    }
}

const webscoket = new WebSocket("ws://" + window.location.host + "/WebScoketServer?name=" + name + "&id=" + time + "&roomid=" + roomid);

// 时间获取
function getCurrentTime() {
    const hour = now.getHours();
    const minute = now.getMinutes();
    return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
}

// XSS处理
function escapeHTML(str) {
    return str.replace(/[&<>"'/]/g, function (c) {
        return "&#" + c.charCodeAt(0) + ";";
    });
}

// 监听来自服务端的消息
webscoket.onmessage = function (event) {
    const messageDiv = document.createElement('div');
    const msg = JSON.parse(event.data);
    const name = msg.name.substring(0, 2);

    if (time == msg.id) {

        messageDiv.classList.add('message');
        messageDiv.innerHTML = `
            <div class="message-box right">
                <span class="time right">${getCurrentTime()}</span>
                <p class="text">
                    ${escapeHTML(msg.message)}
                </p>
            </div>
            <span class="name right">${escapeHTML(name)}</span>
        `;
        chatArea.appendChild(messageDiv);
        chatArea.scrollTop = chatArea.scrollHeight;
    } else {
        messageDiv.classList.add('message');
        messageDiv.innerHTML = `
            <span class="name">${escapeHTML(name)}</span>
            <div class="message-box left">
                <span class="time">${escapeHTML(msg.name)}</span>
                <span class="time">${getCurrentTime()}</span>
                <p class="text">
                    ${escapeHTML(msg.message)}
                </p>
            </div>
        `;
        chatArea.appendChild(messageDiv);
        chatArea.scrollTop = chatArea.scrollHeight;
    }
    console.log(msg);
}

function send() {
    const message = textarea.value;

    if (message.trim() === '') {
        alert('不能发送空白消息');
        return;
    }
    
    // 在 chat-area 里添加 message div 展示发送内容
    webscoket.send(message)

    // 在这里发送消息的逻辑
    console.log(getCurrentTime(),' 发送消息:', message);

    // 清空 textarea
    textarea.value = '';

    // 自动跳到聊天区域尾部
    chatArea.scrollTop = chatArea.scrollHeight;
}

// 按钮发送
sendBtn.addEventListener('click', function() {
    send();
});

// 按键发送
textarea.addEventListener('keydown', function(event) {
    if (event.key === 'Shift') {
        textarea.value += '<br>'; // 处理换行
        return; // 阻止默认行为，避免输入 alt 字符
    }
    if (event.key === 'Enter') { // 
        event.preventDefault(); // 阻止换行

        // 发送消息的逻辑
        const message = textarea.value;
        if (message.trim() !== '') {
            send();
        }
    }
});
