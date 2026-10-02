import { defineConfig } from 'astro/config';
import react from '@astrojs/react';
import tailwindcss from '@tailwindcss/vite';
export default defineConfig({ server:{host:'127.0.0.1',port:4321}, devToolbar:{enabled:false}, integrations:[react()], vite:{plugins:[tailwindcss()],server:{strictPort:true,proxy:{'/api':{target:process.env.BACKEND_URL || 'http://127.0.0.1:9000'}}}} });
