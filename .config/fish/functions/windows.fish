function windows --wraps='sdl-freerdp3 /v:localhost /u:Docker /p:admin /clipboard /dynamic-resolution' --description 'alias windows sdl-freerdp3 /v:localhost /u:Docker /p:admin /clipboard /dynamic-resolution'
    sdl-freerdp3 /v:localhost /u:Docker /p:admin /clipboard /dynamic-resolution $argv
end
