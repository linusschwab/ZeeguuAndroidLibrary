function injectCSS(string) {
    var css = document.createElement("style");
    css.type = "text/css";
    css.innerHTML = string;
    document.head.appendChild(css);
}