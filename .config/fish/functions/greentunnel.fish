function greentunnel --wraps='env -u HTTP_PROXY -u HTTPS_PROXY -u http_proxy -u https_proxy spoofdpi --dns-mode https --dns-https-url "https://1.1.1.1/dns-query"' --description 'alias greentunnel env -u HTTP_PROXY -u HTTPS_PROXY -u http_proxy -u https_proxy spoofdpi --dns-mode https --dns-https-url "https://1.1.1.1/dns-query"'
    env -u HTTP_PROXY -u HTTPS_PROXY -u http_proxy -u https_proxy spoofdpi --dns-mode https --dns-https-url "https://1.1.1.1/dns-query" $argv
end
