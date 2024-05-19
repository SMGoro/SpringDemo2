const sendBtn = document.querySelector('.send-btn');
const textarea = document.querySelector('textarea');
const chatArea = document.getElementById('chat-area');


const now = new Date();
const time = now.getTime();

var webscoket;

function WSConnect(name, time, roomid) {

    // 判断webscoket是否链接，如果链接先断开链接
    if (webscoket !== undefined && webscoket.readyState === WebSocket.OPEN) {
        webscoket.close();
    }

    // 创建新的webscoket链接
    var protocol = window.location.protocol == "https:" ? "wss" : "ws";
    webscoket = new WebSocket(protocol + "://" + window.location.host + "/WebScoketServer?name=" + name + "&id=" + time + "&roomid=" + roomid);
    WSInit();

}

let name;

function GetName() {
    name = document.getElementById("username").value;
    let username = document.getElementById("username");
    let error1 = document.getElementById("error1");

    if (username.value == null || username.value == "" || username.value.length > 10) {
        error1.style.display = "block";
        error1.textContent = `用户名违法！请输入正确的用户名`;
        username.value = '';
        name = null;
        console.error("请输入正确的用户名");
        return;
    } else {
        // error1.style.display = "none";
        name = username.value;
        document.getElementById("login-area").style.display = "none";
        document.getElementById("main-area").style.display = "block";
    }
}

let roomid;

function GetRoomID() {
    // roomid = prompt("请输入加入房间");
    roomid = document.getElementById("roomid").value;
    if (roomid == null || roomid == "") {
        roomid = "public"
        document.getElementById("title").innerHTML = `<h1>${roomid}</h1>`;
        WSConnect(name, time, roomid);
    } else {
        document.getElementById("title").innerHTML = `<h1>${roomid}</h1>`;
        WSConnect(name, time, roomid);
    }
}

function login() {
    GetName();
    GetRoomID();
}

function logout() {
    document.getElementById("login-area").style.display = "grid";
    document.getElementById("main-area").style.display = "none";
    webscoket.close();
}

function isBase64(str) {
    const base64Pattern = /^data:image\/([a-zA-Z]+);base64,([0-9a-zA-Z+/=]+)$/;
    return base64Pattern.test(str);
}

function WSInit() {
    // 监听来自服务端的消息
    webscoket.onmessage = function (event) {
        const messageDiv = document.createElement('div');
        const msg = JSON.parse(event.data);
        let type;

        if (isBase64(msg.message)) {
            type = "image";
        } else {
            type = "text";
        }

        if (time == msg.id) {
            TypeSend(type, escapeHTML(msg.name), escapeHTML(msg.message), "right", formatDate(msg.time));
        } else {
            TypeSend(type, escapeHTML(msg.name), escapeHTML(msg.message), "left", formatDate(msg.time));
        }
        console.log(msg);
    }

    webscoket.onopen = function (event) {

    }

    webscoket.onclose = function (event) {
        console.log('WebSocket closed:', event);
    }

    webscoket.onerror = function (event) {
        console.error('WebSocket error:', event);
    }
}

// 时间获取
function getCurrentTime() {
    const hour = now.getHours();
    const minute = now.getMinutes();
    return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
}

function formatDate(dateStr) {
    let now = new Date();
    let date = new Date(dateStr);

    let yearOptions = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
    let monthDayOptions = { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
    let timeOptions = { hour: '2-digit', minute: '2-digit' };

    if (date.getFullYear() !== now.getFullYear()) {
        return date.toLocaleString([], yearOptions);
    } else if (date.getMonth() === now.getMonth() && date.getDate() === now.getDate()) {
        return date.toLocaleString([], timeOptions);
    } else {
        return date.toLocaleString([], monthDayOptions);
    }
}

// XSS处理
function escapeHTML(str) {
    return str.replace(/[&<>"'/]/g, function (c) {
        return "&#" + c.charCodeAt(0) + ";";
    });
}

// 发送信息
function TypeSend(type, name, msg, pos, time) {
    const messageDiv = document.createElement('div');
    let showmsg;
    if (type == "text") {
        showmsg = msg;
    } else if (type == "image") {
        showmsg = `<img src="${msg}" alt="image" class="upload-image">`;
    }
    if (pos == "left") {
        messageDiv.classList.add('message');
        messageDiv.innerHTML = `
            <span class="name">${name.substring(0, 2)}</span>
            <div class="message-box left">
                <span class="fullname">${name}</span>
                <span class="time">${time}</span>
                <p class="text">
                    ${showmsg}
                </p>
            </div>
        `;
        chatArea.appendChild(messageDiv);
        chatArea.scrollTop = chatArea.scrollHeight;
    } else if (pos == "right") {
        messageDiv.classList.add('message');
        messageDiv.innerHTML = `
            <div class="message-box right">
                <span class="time right">${time}</span>
                <p class="text">
                    ${showmsg}
                </p>
            </div>
            <span class="name right">${name.substring(0, 2)}</span>
        `;
        chatArea.appendChild(messageDiv);
        chatArea.scrollTop = chatArea.scrollHeight;
    }

}

function send() {
    const message = textarea.value;
    const messageDiv = document.createElement('div');

    if (message.trim() === '') {
        alert('不能发送空白消息');
        return;
    } else if (webscoket.readyState !== WebSocket.OPEN) {
        // alert('WebSocket未连接，请稍后再试');
        TypeSend("text", "Error", "WebSocket未连接，请稍后再试", "left");
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

// 图片上传
document.getElementById('fileInput').addEventListener('change', readFile, false);

function readFile(event) {
    const file = event.target.files[0];
    if (!file) {
        console.log('No file selected.');
        return;
    }
    // 计算文件大小（单位：字节）
    const fileSize = file.size;

    // 将文件大小转换为千字节（1kb = 1024字节）
    const fileSizeInKB = fileSize / 1024;

    // 判断文件大小是否超过100kb
    if (fileSizeInKB > 100) {
        alert('图片大小不能超过100kb');
        return;
    }
    const reader = new FileReader();
    reader.onload = function(e) {
        const base64Image = e.target.result;
        console.log(base64Image);
        webscoket.send(base64Image)
        // TypeSend("image", name, base64Image, "right");
    };
    reader.readAsDataURL(file);
}


