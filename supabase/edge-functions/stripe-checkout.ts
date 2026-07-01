import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import Stripe from 'https://esm.sh/stripe@14?target=denonext'
//REQUIRED ENVIRONMENT VARIABLES:
//FRONTEND_URL = website domain url
//STRIPE_API_KEY = your stripe api key

//for web
const corsHeaders = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type, x-region',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
}

const projectUrl = Deno.env.get('FRONTEND_URL') ?? 'http://localhost:8080';

const stripe = new Stripe(Deno.env.get('STRIPE_API_KEY') ?? '', {
    apiVersion: '2026-04-22.dahlia',
    httpClient: Stripe.createFetchHttpClient(),
});

serve(async (req) => {

    //for web
    if (req.method === 'OPTIONS') {
        return new Response('ok', { headers: corsHeaders })
    }

    const { items } = await req.json(); // Array of { price: string, quantity: number }

    const itemMetadata = items.reduce((acc: any, item: any) => {
        // Stripe metadata values MUST be strings
        acc[item.price] = item.quantity.toString();
        return acc;
    }, {});

    try {
        const session = await stripe.checkout.sessions.create({
            payment_method_types: ['card'],
            line_items: items, // Stripe expects an array here
            metadata: itemMetadata,
            mode: 'payment',
            shipping_address_collection: {
                allowed_countries: ['CA'] // Specify allowed countries here
            },
            success_url: `${projectUrl}/success`,
            cancel_url: `${projectUrl}/cancel`,
        });

        return new Response(JSON.stringify({ url: session.url }), {
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
            status: 200,
        });
    } catch (error) {
        return new Response(JSON.stringify({ error: error.message }), {
            headers: { ...corsHeaders, 'Content-Type': 'application/json' },
            status: 400
        });
    }
})